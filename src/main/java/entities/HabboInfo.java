package entities;

import javafx.scene.image.Image;
import message.JSONSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HabboInfo implements JSONSerializable {

    private int id;
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

    public Image getHeadImg() {
        return headImg;
    }

    public void setHeadImg(Image headImg) {
        this.headImg = headImg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHabboName() {
        return habboName;
    }

    public void setHabboName(String habboName) {
        this.habboName = habboName;
    }

    public String getFigureStr() {
        return figureStr;
    }

    public void setFigureStr(String figureStr) {
        this.figureStr = figureStr;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public boolean imageLoading() {
        return this.imageLoading;
    }

    public void setImageLoading(boolean b) {
        this.imageLoading = b;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
