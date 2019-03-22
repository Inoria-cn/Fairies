package org.squirrelnest.fairies.dto;

/**
 * Created by Inoria on 2019/3/17.
 */
public abstract class AbstractResult {
    public static final int RETURN_CODE_SUCCESS = 0;
    public static final int RETURN_CODE_VALUE_NOT_FOUNT = 1;
    public static final int RETURN_CODE_PARAM_ERROR = 11;
    public static final int RETURN_CODE_FAILED = -1;

    private Integer returnCode;
    private String returnMessage;

    public AbstractResult() {
        this.returnCode = RETURN_CODE_SUCCESS;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public Boolean success() {
        return returnCode == RETURN_CODE_SUCCESS;
    }
}
