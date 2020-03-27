package com.wolfmobileapps.inwentaryzacja;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class OverViewItem {

    Bitmap pictureSQLData;
    String idSQLData;
    String dateSQLData;
    String descriptionSQLData;

    public OverViewItem(Bitmap pictureSQLData, String idSQLData, String dateSQLData, String descriptionSQLData) {
        this.pictureSQLData = pictureSQLData;
        this.idSQLData = idSQLData;
        this.dateSQLData = dateSQLData;
        this.descriptionSQLData = descriptionSQLData;
    }

    public Bitmap getPictureSQLData() {
        return pictureSQLData;
    }

    public void setPictureSQLData(Bitmap pictureSQLData) {
        this.pictureSQLData = pictureSQLData;
    }

    public String getIdSQLData() {
        return idSQLData;
    }

    public void setIdSQLData(String idSQLData) {
        this.idSQLData = idSQLData;
    }

    public String getDateSQLData() {
        return dateSQLData;
    }

    public void setDateSQLData(String dateSQLData) {
        this.dateSQLData = dateSQLData;
    }

    public String getDescriptionSQLData() {
        return descriptionSQLData;
    }

    public void setDescriptionSQLData(String descriptionSQLData) {
        this.descriptionSQLData = descriptionSQLData;
    }
}
