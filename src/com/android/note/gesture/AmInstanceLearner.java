package com.android.note.gesture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

public class AmInstanceLearner extends AmLearner {
	   private static final Comparator<AmPrediction> sComparator = new Comparator<AmPrediction>() {
	        public int compare(AmPrediction object1, AmPrediction object2) {
	            double score1 = object1.score;
	            double score2 = object2.score;
	            if (score1 > score2) {
	                return -1;
	            } else if (score1 < score2) {
	                return 1;
	            } else {
	                return 0;
	            }
	        }
	    };

	    @Override
	    ArrayList<AmPrediction> classify(int sequenceType, int orientationType, float[] vector) {
	        ArrayList<AmPrediction> predictions = new ArrayList<AmPrediction>();
	        ArrayList<AmInstance> instances = getInstances();
	        int count = instances.size();
	        TreeMap<String, Double> label2score = new TreeMap<String, Double>();
	        for (int i = 0; i < count; i++) {
	            AmInstance sample = instances.get(i);
	            if (sample.vector.length != vector.length) {
	                continue;
	            }
	            double distance;
	            if (sequenceType == AmGestureStore.SEQUENCE_SENSITIVE) {
	                distance = AmGestureUtils.minimumCosineDistance(sample.vector, vector, orientationType);
	            } else {
	                distance = AmGestureUtils.squaredEuclideanDistance(sample.vector, vector);
	            }
	            double weight;
	            if (distance == 0) {
	                weight = Double.MAX_VALUE;
	            } else {
	                weight = 1 / distance;
	            }
	            Double score = label2score.get(sample.label);
	            if (score == null || weight > score) {
	                label2score.put(sample.label, weight);
	            }
	        }

//	        double sum = 0;
	        for (String name : label2score.keySet()) {
	            double score = label2score.get(name);
//	            sum += score;
	            predictions.add(new AmPrediction(name, score));
	        }

	        // normalize
//	        for (Prediction prediction : predictions) {
//	            prediction.score /= sum;
//	        }

	        Collections.sort(predictions, sComparator);

	        return predictions;
	    }
}
