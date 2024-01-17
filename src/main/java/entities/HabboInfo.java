package entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import message.JSONSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HabboInfo implements JSONSerializable {

    private int id;
    private int index; // the id you get inside a room
    private String habboName;
    private String figureStr;
    private String sex;
    private String mission;
    private Hotel hotel;
    private Image headImg;
    private boolean imageLoading;

    public HabboInfo() {
        this.imageLoading = false;
    }

    public HabboInfo(int id, String habboName, String figureStr, String sex, String mission, Hotel hotel) {
        this();
        this.id = id;
        this.habboName = habboName;
        this.figureStr = figureStr;
        this.sex = sex;
        this.mission = mission;
        this.hotel = hotel;
    }

    public HabboInfo(String habboName, String figureStr, String sex, String mission, Hotel hotel) {
        this(-1, habboName, figureStr, sex, mission, hotel);
    }


    public ImageView getHabboHeadImage() {
        ImageView userImgView = new ImageView();
        userImgView.setPreserveRatio(true);
        userImgView.setFitWidth(35);

        if(this.headImg == null) {
            userImgView.setImage(new Image("/avatar-head-placeholder.png"));
            if(!imageLoading){
                Image img = new Image(getFigureStringUrl(this.figureStr), true);
                img.progressProperty().addListener((observable, oldValue, progress) -> {
                    this.imageLoading = true;
                    if ((Double) progress == 1.0 && !img.isError()) {
                        this.headImg = img;
                        this.imageLoading = false;
                        userImgView.setImage(img);
                    }
                });
            }
        }else {
            userImgView.setImage(this.headImg);
        }
        return userImgView;
    }

    private static String getFigureStringUrl(String figure) {
        return "https://www.habbo.com/habbo-imaging/avatarimage?size=b&figure="+figure+"&headonly=1";
    }

    @Override
    public String toString() {
        return "entities.HabboInfo{" +
                "id=" + id +
                ", habboName='" + habboName + '\'' +
                ", figureStr='" + figureStr + '\'' +
                ", sex='" + sex + '\'' +
                ", mission='" + mission + '\'' +
                ", hotel='" + hotel + '\'' +
                '}';
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap();
		map.put("name", this.habboName);
		map.put("mission", this.mission);
		map.put("figure", this.figureStr);
		map.put("sex", this.sex);
		map.put("hotel", this.hotel);
		return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabboInfo habboInfo = (HabboInfo) o;
        return habboName.equals(habboInfo.habboName) &&
                hotel == habboInfo.hotel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(habboName, hotel);
    }

    public String getHabboName() {
        return habboName;
    }

    public String getFigureStr() {
        return figureStr;
    }

    public String getSex() {
        return sex;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
