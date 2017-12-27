package kingkong.facerecognize.app.entity;

import java.util.List;

/**
 * Created by KingKong-HE on 2017/12/27.
 *
 * @author KingKong-HE
 * @Time 2017/12/27
 * @Email 709872217@QQ.COM
 */
public class VoiceprintPasswordInfo {

    /**
     * ssub : ivp
     * sst : download
     * num_pwd : ["72043986","07536894","47293506","08597423","35802697"]
     */

    private String ssub;
    private String sst;
    private List<String> num_pwd;

    public String getSsub() {
        return ssub;
    }

    public void setSsub(String ssub) {
        this.ssub = ssub;
    }

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }

    public List<String> getNum_pwd() {
        return num_pwd;
    }

    public void setNum_pwd(List<String> num_pwd) {
        this.num_pwd = num_pwd;
    }
}
