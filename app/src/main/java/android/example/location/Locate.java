package android.example.location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location_table")
public class Locate {

    @PrimaryKey(autoGenerate = true)private int id;

    @NonNull
    private@ColumnInfo(name = "locate")String Locate;

    public Locate(){}
    public Locate(String locate) {
        this.Locate = locate;
    }
    public void setLocate(String locate) {
        this.Locate=locate;
    }


    public String getLocate() {
        return this.Locate;
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
    }
}
