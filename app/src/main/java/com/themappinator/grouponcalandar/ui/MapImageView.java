package com.themappinator.grouponcalandar.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.model.Room;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ayegorov on 12/2/15.
 */
public class MapImageView extends ImageView {

    private static final int SELECTED_COLOR = Color.parseColor("#15A5EB");
    private static final HashMap<String, Integer> floorToResourceIdMap = new HashMap<>();

    private ArrayList<Room> selectedRooms = new ArrayList<>();
    private String currentFloor;

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        floorToResourceIdMap.put("pa5_1", R.drawable.portola_map);
        floorToResourceIdMap.put("pa4_1", R.drawable.park_first_floor_map);
        floorToResourceIdMap.put("pa4_2", R.drawable.park_second_floor_map);
        floorToResourceIdMap.put("pa4_3", R.drawable.park_third_floor_map);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable();

        // TODO: remove this check when we add all the maps (or put an assert) (AvY)
        if (vectorDrawable != null) {
            Matrix ctmMatrix = calculateCTMMatrix();
            for (int i = 0; i < selectedRooms.size(); ++i) {
                drawRoom(canvas, ctmMatrix, selectedRooms.get(i));
            }
        }
    }

    //
    // -- Helpers
    //
    private Matrix calculateCTMMatrix() {

        Rect svgBounds = getViewPortBounds();
        Rect bounds = getDrawable().getBounds();
        Matrix imageMatrix = getImageMatrix();

        float scaleX = (float) bounds.width() / svgBounds.width();
        float scaleY = (float) bounds.height() / svgBounds.height();

        Matrix finalMatrix = new Matrix(imageMatrix);
        float imageMatrixValues[] = new float[9];
        imageMatrix.getValues(imageMatrixValues);
        finalMatrix.setScale(imageMatrixValues[Matrix.MSCALE_X] * scaleX, imageMatrixValues[Matrix.MSCALE_Y] * scaleY);
        finalMatrix.postTranslate(imageMatrixValues[Matrix.MTRANS_X], imageMatrixValues[Matrix.MTRANS_Y]);

        return finalMatrix;
    }

    private Rect getViewPortBounds() {

        Rect viewportBounds = new Rect();

        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable();

        try {
            Field vectorDrawableStateField = vectorDrawable.getClass().getDeclaredField("mVectorState");
            vectorDrawableStateField.setAccessible(true);
            Object vectorDrawableState = vectorDrawableStateField.get(vectorDrawable);
            Field pathRendererField = vectorDrawableState.getClass().getDeclaredField("mVPathRenderer");
            pathRendererField.setAccessible(true);
            Object pathRenderer = pathRendererField.get(vectorDrawableState);
            Field viewportWidthField = pathRenderer.getClass().getDeclaredField("mViewportWidth");
            viewportWidthField.setAccessible(true);
            Field viewportHeightField = pathRenderer.getClass().getDeclaredField("mViewportHeight");
            viewportHeightField.setAccessible(true);

            Float viewportWidth = (Float)viewportWidthField.get(pathRenderer);
            Float viewportHeight = (Float)viewportHeightField.get(pathRenderer);

            viewportBounds.set(0, 0, viewportWidth.intValue(), viewportHeight.intValue());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return viewportBounds;
    }

    private void drawRoom(Canvas canvas, Matrix ctmMatrix, Room room) {

        Path path = getPathForRoom(room);

        if (!path.isEmpty()) {
            path.transform(ctmMatrix);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(SELECTED_COLOR);
            canvas.drawPath(path, paint);
        }
    }

    private Path getPathForRoom(Room room) {
        Path roomPath = new Path();

        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable();
        try {
            Method getTargetByNameMethod = vectorDrawable.getClass().getDeclaredMethod("getTargetByName", String.class);
            getTargetByNameMethod.setAccessible(true);
            Object vPathObject = getTargetByNameMethod.invoke(vectorDrawable, room.roomid);
            if (vPathObject != null) {
                Method toPathMethod = vPathObject.getClass().getMethod("toPath", Path.class);
                toPathMethod.setAccessible(true);
                toPathMethod.invoke(vPathObject, roomPath);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return roomPath;
    }

    //
    // -- Interface
    //

    public void selectRoom(Room selectedRoom) {
        assert(selectedRoom != null);
        this.selectedRooms.add(selectedRoom);

        if (currentFloor == null || !currentFloor.equals(selectedRoom.floor)) {
            currentFloor = selectedRoom.floor;

            Integer mapResourceId = floorToResourceIdMap.get(selectedRoom.floor);
            if (mapResourceId != null) {
                setImageResource(mapResourceId);
            }
        }

        // TODO: replace with invalidate(l,t,r,b) for efficiency (AvY)
        this.postInvalidate();
    }

    public void deselectRoom(Room deselectedRoom) {
        assert(deselectedRoom != null);

        selectedRooms.remove(deselectedRoom);
        // TODO: replace with invalidate(l,t,r,b) for efficiency (AvY)
        this.postInvalidate();
    }
}
