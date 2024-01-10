package utils.Tasks;

import Entity.MilitarType;

public class ApproveTask {

    private final String task;

    private final String approvedUsername;

    private final MilitarType approvedUserRole;


    public ApproveTask(String task, String approvedUsername, MilitarType approvedUserRole) {
        this.task = task;
        this.approvedUsername = approvedUsername;
        this.approvedUserRole = approvedUserRole;
    }


    public String getTask() {
        return task;
    }

    public String getApprovedUsername() {
        return approvedUsername;
    }

    public MilitarType getApprovedUserRole() {
        return approvedUserRole;
    }
}
