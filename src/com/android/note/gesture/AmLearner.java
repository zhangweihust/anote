package com.android.note.gesture;

import java.util.ArrayList;

abstract class AmLearner {
    private final ArrayList<AmInstance> mInstances = new ArrayList<AmInstance>();

    /**
     * Add an instance to the learner
     * 
     * @param instance
     */
    void addInstance(AmInstance instance) {
        mInstances.add(instance);
    }

    /**
     * Retrieve all the instances
     * 
     * @return instances
     */
    ArrayList<AmInstance> getInstances() {
        return mInstances;
    }

    /**
     * Remove an instance based on its id
     * 
     * @param id
     */
    void removeInstance(long id) {
        ArrayList<AmInstance> instances = mInstances;
        int count = instances.size();
        for (int i = 0; i < count; i++) {
        	AmInstance instance = instances.get(i);
            if (id == instance.id) {
                instances.remove(instance);
                return;
            }
        }
    }

    /**
     * Remove all the instances of a category
     * 
     * @param name the category name
     */
    void removeInstances(String name) {
        final ArrayList<AmInstance> toDelete = new ArrayList<AmInstance>();
        final ArrayList<AmInstance> instances = mInstances;
        final int count = instances.size();

        for (int i = 0; i < count; i++) {
            final AmInstance instance = instances.get(i);
            // the label can be null, as specified in Instance
            if ((instance.label == null && name == null)
                    || (instance.label != null && instance.label.equals(name))) {
                toDelete.add(instance);
            }
        }
        instances.removeAll(toDelete);
    }

    abstract ArrayList<AmPrediction> classify(int sequenceType, int orientationType, float[] vector);
}
