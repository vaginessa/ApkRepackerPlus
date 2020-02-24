package com.mrikso.apkrepacker.ui.colorslist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.jfenn.colorpickerdialog.views.CircleColorView;

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {
    public boolean changed;
    private LayoutInflater mInflater;
    private List<ColorMeta> mColors;
    private OnItemInteractionListener mListener;

    public ColorsAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setData(List<ColorMeta> colors) {
        mColors = colors;
        notifyDataSetChanged();
    }

    public void newValue(int position, String color, String name) {
        ColorMeta colorMeta = new ColorMeta.Builder(name)
                .setValue(color)
                .setIcon(color)
                .build();
        mColors.set(position, colorMeta);
        notifyDataSetChanged();
    }
/**
 * При фильтре цветов почему-то возвражается null, нужно исправить, но мне как-то уже впадло
 */
    public String getValue(String key) {
        List<ColorMeta> colors = new ArrayList<>(mColors);
        for (ColorMeta colorMeta : colors) {
            String stringId = colorMeta.label;
            String stringText = colorMeta.value;
            if (stringId.equals(key)) {
                return stringText;
            }
        }
        return null;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setInteractionListener(OnItemInteractionListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        changed = true;
        ColorMeta packageMeta = mColors.get(position);
        holder.bindTo(packageMeta);
    }

    @Override
    public int getItemCount() {
        return mColors == null ? 0 : mColors.size();
    }

    @Override
    public long getItemId(int position) {
        return mColors.get(position).hashCode();
    }

    public int getColor(String colorValue) {
        if (colorValue.startsWith("@color/")) {
            String substring = colorValue.substring(7);
            Log.d("Color","value: " + substring);
            Log.d("Color", "color: " + getValue(substring));
            String rerurnedValue = getValue(substring);
            if(rerurnedValue != null) {
                if (rerurnedValue.startsWith("@android:color/") | rerurnedValue.startsWith("@color/")) {
                    //да зраствует великая рекурсия
                    getColor(rerurnedValue);
                } else {
                    return Color.parseColor(rerurnedValue);
                }
            }
        } else if (colorValue.startsWith("@android:color/")) {
            Object color = getAndroidColor("android.R$color", colorValue.substring(15));
            if (color != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return App.getContext().getColor((Integer) color);
                }
            }
        }
        else {
            return Color.parseColor(colorValue);
        }
        return 0;
    }

    /**
     * C помощью рефлексии досятаем цвет вида @android:color/white
     * @param clazz имя класса android.R$color
     * @param colorName имя цвета white
     * @return результат #ffffffff
     */

    public  Object getAndroidColor(String clazz, String colorName){
        Field declaratedField;
        try {
            declaratedField = Class.forName(clazz).getField(colorName);
            declaratedField.setAccessible(true);
            return declaratedField.get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnItemInteractionListener {
        void onColorClicked(ColorMeta color, int id);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mColorName;
        private TextView mcolorValue;
        private CircleColorView mColorIcon;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mColorName = itemView.findViewById(R.id.tv_color_name);
            mcolorValue = itemView.findViewById(R.id.tv_color_value);
            mColorIcon = itemView.findViewById(R.id.tv_color_icon);

            itemView.findViewById(R.id.app_item).setOnClickListener((v) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                if (mListener != null)
                    mListener.onColorClicked(mColors.get(adapterPosition), adapterPosition);
            });
        }

        @SuppressLint("DefaultLocale")
        void bindTo(ColorMeta colorMeta) {
            mColorName.setText(colorMeta.label);
            mcolorValue.setText(colorMeta.value);
            mColorIcon.setColor(getColor(colorMeta.value));
        }
    }

}
