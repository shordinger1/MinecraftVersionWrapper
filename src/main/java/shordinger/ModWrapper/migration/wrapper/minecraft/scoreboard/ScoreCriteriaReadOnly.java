package shordinger.ModWrapper.migration.wrapper.minecraft.scoreboard;

public class ScoreCriteriaReadOnly extends ScoreCriteria {

    public ScoreCriteriaReadOnly(String name) {
        super(name);
    }

    public boolean isReadOnly() {
        return true;
    }
}
