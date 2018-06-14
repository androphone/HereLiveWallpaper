package pl.czerniak.kamil.herelivewallpaper;


/**
 * PreferenceSet from project HereLiveWallpaper.
 * Created by kamil on 07/06/2017.
 */

class PreferenceSet {
    private int mZoom = 19;
    private int mType = 0;
    private int mRefresh = 10;
    private float mDisplacement = 10.0f;

    PreferenceSet(int mZoom, int mType, int mRefresh, float mDisplacement) {
        this.mZoom = mZoom;
        this.mType = mType;
        this.mRefresh = mRefresh;
        this.mDisplacement = mDisplacement;
    }

    int getZoom() {
        return mZoom;
    }

    int getRefreshInterval() {
        return mRefresh * 1000 * 60;
    }

    float getDisplacement() {
        return mDisplacement;
    }

    String parseTypeBing() {
        switch (mType) {
            case 0:
                return "Road";
            case 1:
                return "Aerial";
            case 2:
                return "AerialWithLabels";
            default:
                return "Road";
        }
    }

    String parseTypeMapBox() {
        switch (mType) {
            case 0:
                return "streets-v10";
            case 1:
                return "outdoors-v10";
            case 2:
                return "light-v9";
            case 3:
                return "dark-v9";
            case 4:
                return "satellite-v9";
            case 5:
                return "satellite-streets-v10";
            case 6:
                return "traffic-day-v2";
            case 7:
                return "traffic-night-v2";
            default:
                return "streets-v10";
        }
    }


    void setZoom(int mZoom) {
        if (mZoom == this.mZoom) {
            return;
        }
        this.mZoom = mZoom;
    }

    void setType(int mType) {
        if (mType == this.mType) {
            return;
        }
        this.mType = mType;
    }

    void setRefresh(int mRefresh) {
        if (mRefresh == this.mRefresh) {
            return;
        }
        this.mRefresh = mRefresh;
    }

    void setDisplacement(float mDisplacement) {
        if (mDisplacement == this.mDisplacement) {
            return;
        }
        this.mDisplacement = mDisplacement;
    }

}
