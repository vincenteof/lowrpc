package rpc.protocol;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 11:19
 */
public class RpcResponse {
    private Integer status;
    private String description;
    private Object value;
    private Integer requestId;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
            "status=" + status +
            ", description='" + description + '\'' +
            ", value=" + value +
            ", requestId=" + requestId +
            '}';
    }
}
