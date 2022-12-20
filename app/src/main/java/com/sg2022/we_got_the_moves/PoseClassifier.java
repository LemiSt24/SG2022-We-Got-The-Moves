package com.sg2022.we_got_the_moves;

import android.content.Context;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.sg2022.we_got_the_moves.FullBodyPoseEmbedder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;

class PoseSample {
    public double[] embedding;
    public String class_name;
    public PoseSample(double[] emb, String name)
    {
        embedding = emb;
        class_name = name;
    }
}

class EmbeddingDistance {
    public double distance;
    public int index;
    public EmbeddingDistance(int i, double d)
    {
        index = i;
        distance = d;
    }
}

class EmbeddingDistanceComparator implements Comparator<EmbeddingDistance> {
    @Override
    public int compare(EmbeddingDistance o1, EmbeddingDistance o2)
    {
        return Double.compare(o1.distance, o2.distance);
    }
}

class ClassificationResult {
    public double min_distance;
    public NormalizedLandmark[] landmarks;
    public double[] embedding;
    public Map<String, Integer> result;
}

/*
    Ein kNN-Klassifikator für pose embeddings.
 */
public class PoseClassifier {

    private Context context;
    private int top_n_by_max_distance;
    private int top_n_by_mean_distance;
    private String dataset_filename;

    private List<PoseSample> dataset;
    private List<String> class_names;

    private List<ClassificationResult> classification_history;
    private int history_depth = 10; // wie viele Schritte kann man in die Vergangenheit blicken?

    double[] current_embedding;

    public PoseClassifier(Context myContext, int top_n_max, int top_n_mean, String filename)
    {
        dataset = new ArrayList<PoseSample>();
        class_names = new ArrayList<String>();

        classification_history = new ArrayList<ClassificationResult>();

        context = myContext;
        top_n_by_max_distance = top_n_max;
        top_n_by_mean_distance = top_n_mean;
        dataset_filename = filename;
        load_pose_samples();
    }

    private void load_pose_samples()
    {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(context.getAssets()
                    .open(dataset_filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(is);
        String line;
        StringTokenizer st = null;
        try {
            while ((line = reader.readLine()) != null) {
                st = new StringTokenizer(line, ",");
                double[] embedding = new double[FullBodyPoseEmbedder.getEmbedding_size()];

                st.nextToken(); // Starte bei 1, da die ersten Spalte den ursprünglichen Dateinamen enthält
                String class_name = st.nextToken();
                for(int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++)
                {
                    embedding[i] = Float.parseFloat(st.nextToken());
                }
                dataset.add(new PoseSample(embedding, class_name));
                if(!class_names.contains(class_name))
                {
                    class_names.add(class_name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> get_result()
    {
        return get_result(0);
    }

    // index zählt von "oben", also ist 0 das neuste Ergebnis, 1 das zweitneuste, etc.
    public Map<String, Integer> get_result(int index)
    {
        if(classification_history.size() > index)
            return classification_history.get(classification_history.size() - 1 - index).result;
        else
            return new HashMap<>();
    }

    public void classify(LandmarkProto.NormalizedLandmarkList landmarks)
    {
        NormalizedLandmark[] normalized_landmarks = NormalizedLandmark.convertFromMediapipe(landmarks);

        double[] embedding = FullBodyPoseEmbedder.generate_embedding(normalized_landmarks);
        double[] embedding_flipped = FullBodyPoseEmbedder.generate_embedding(normalized_landmarks, true);

        /* Filter by max distance.
         This helps to remove outliers - poses that are almost the same as the
         given one, but has one joint bent into another direction and actually
         represent a different pose class.
        */
        List<EmbeddingDistance> max_dist_heap = new ArrayList<EmbeddingDistance>();
        int index = 0;
        for(PoseSample sample : dataset)
        {
            double dist_to_embedding = 0.0;
            double dist_to_flipped_embedding = 0.0;
            for(int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++)
            {
                dist_to_embedding = max(abs(sample.embedding[i] - embedding[i]), dist_to_embedding);
                dist_to_flipped_embedding = max(abs(sample.embedding[i] - embedding_flipped[i]), dist_to_flipped_embedding);
            }
            max_dist_heap.add(new EmbeddingDistance(index++, min(dist_to_embedding, dist_to_flipped_embedding)));
        }
        Collections.sort(max_dist_heap, new EmbeddingDistanceComparator());

        while (max_dist_heap.size() > top_n_by_max_distance) {
            max_dist_heap.remove(max_dist_heap.size() - 1);
        }

        /*
        Filter by mean distance.
        After removing outliers we can find the nearest pose by mean distance.
         */
        List<EmbeddingDistance> mean_dist_heap = new ArrayList<EmbeddingDistance>();
        for(EmbeddingDistance dist : max_dist_heap)
        {
            PoseSample sample = dataset.get(dist.index);
            double dist_to_embedding = 0.0;
            double dist_to_flipped_embedding = 0.0;
            for(int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++)
            {
                dist_to_embedding += abs(sample.embedding[i] - embedding[i]);
                dist_to_flipped_embedding += abs(sample.embedding[i] - embedding_flipped[i]);
            }
            dist_to_embedding /= FullBodyPoseEmbedder.getEmbedding_size();
            dist_to_flipped_embedding /= FullBodyPoseEmbedder.getEmbedding_size();
            mean_dist_heap.add(new EmbeddingDistance(dist.index, min(dist_to_embedding, dist_to_flipped_embedding)));
        }
        Collections.sort(mean_dist_heap, new EmbeddingDistanceComparator());

        while (mean_dist_heap.size() > top_n_by_mean_distance) {
            mean_dist_heap.remove(mean_dist_heap.size() - 1);
        }

        Map<String, Integer> ret = new HashMap<String, Integer>();
        for(EmbeddingDistance dist : mean_dist_heap)
        {
            String class_name = dataset.get(dist.index).class_name;
            if(!ret.containsKey(class_name))
                ret.put(class_name, 0);
            ret.put(class_name, ret.get(class_name) + 1);
        }
        //return ret;
        ClassificationResult res = new ClassificationResult();
        res.landmarks = normalized_landmarks;
        res.embedding = embedding;
        res.result = ret;
        res.min_distance = mean_dist_heap.get(0).distance;
        classification_history.add(res);

        if(classification_history.size() > history_depth)
        {
            classification_history.remove(0);
        }
    }

    // Bewertet die zuletzt übermittelte Pose bezüglich Einschränkungen wie Abstand der Füße etc
    // Rückgabewert ist eine Distanz (z.B. der Unterschied zwischen Fußabstand und Schulterabstand)
    // erwartet (eine Liste von) Strings pro Argument, falls zwei genannt: Landmarks werden gemittelt
    public double get_distance(String from, String to)
    {
        NormalizedLandmark[] landmarks = classification_history.get(classification_history.size() - 1).landmarks;

        NormalizedLandmark lmfrom;
        // mehrere Landmarks in "from"
        if(from.contains(","))
        {
            String[] spl = from.split(",");
            lmfrom = NormalizedLandmark.getAverageByNames(landmarks, spl[0], spl[1]);
        }
        else
        {
            lmfrom = landmarks[NormalizedLandmark.landmark_names.indexOf(from)];
        }

        NormalizedLandmark lmto;
        // mehrere Landmarks in "from"
        if(to.contains(","))
        {
            String[] spl = to.split(",");
            lmto = NormalizedLandmark.getAverageByNames(landmarks, spl[0], spl[1]);
        }
        else
        {
            lmto = landmarks[NormalizedLandmark.landmark_names.indexOf(to)];
        }

        return lmfrom.getDistance(lmto);
    }
}
