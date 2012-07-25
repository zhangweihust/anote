package com.archermind.note.gesture;

public class AmInstance {
    private static final int SEQUENCE_SAMPLE_SIZE = 16;

    private static final int PATCH_SAMPLE_SIZE = 16;

    private final static float[] ORIENTATIONS = {
            0, (float) (Math.PI / 4), (float) (Math.PI / 2), (float) (Math.PI * 3 / 4),
            (float) Math.PI, -0, (float) (-Math.PI / 4), (float) (-Math.PI / 2),
            (float) (-Math.PI * 3 / 4), (float) -Math.PI
    };

    // the feature vector
    final float[] vector;

    // the label can be null
    final String label;

    // the id of the instance
    final long id;

    private AmInstance(long id, float[] sample, String sampleName) {
        this.id = id;
        vector = sample;
        label = sampleName;
    }

    private void normalize() {
        float[] sample = vector;
        float sum = 0;

        int size = sample.length;
        for (int i = 0; i < size; i++) {
            sum += sample[i] * sample[i];
        }

        float magnitude = (float)Math.sqrt(sum);
        for (int i = 0; i < size; i++) {
            sample[i] /= magnitude;
        }
    }

    /**
     * create a learning instance for a single stroke gesture
     * 
     * @param gesture
     * @param label
     * @return the instance
     */
    static AmInstance createInstance(int sequenceType, int orientationType, AmGesture gesture, String label) {
        float[] pts;
        AmInstance instance;
        if (sequenceType == AmGestureStore.SEQUENCE_SENSITIVE) {
            pts = temporalSampler(orientationType, gesture);
            instance = new AmInstance(gesture.getID(), pts, label);
            instance.normalize();
        } else {
            pts = spatialSampler(gesture);
            instance = new AmInstance(gesture.getID(), pts, label);
        }
        return instance;
    }

    private static float[] spatialSampler(AmGesture gesture) {
        return AmGestureUtils.spatialSampling(gesture, PATCH_SAMPLE_SIZE, false);
    }

    private static float[] temporalSampler(int orientationType, AmGesture gesture) {
        float[] pts = AmGestureUtils.temporalSampling(gesture.getStrokes().get(0),
                SEQUENCE_SAMPLE_SIZE);
        float[] center = AmGestureUtils.computeCentroid(pts);
        float orientation = (float)Math.atan2(pts[1] - center[1], pts[0] - center[0]);

        float adjustment = -orientation;
        if (orientationType != AmGestureStore.ORIENTATION_INVARIANT) {
            int count = ORIENTATIONS.length;
            for (int i = 0; i < count; i++) {
                float delta = ORIENTATIONS[i] - orientation;
                if (Math.abs(delta) < Math.abs(adjustment)) {
                    adjustment = delta;
                }
            }
        }

        AmGestureUtils.translate(pts, -center[0], -center[1]);
        AmGestureUtils.rotate(pts, adjustment);

        return pts;
    }


}
