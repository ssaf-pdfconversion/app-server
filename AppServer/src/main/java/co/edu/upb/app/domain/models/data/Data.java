package co.edu.upb.app.domain.models.data;

public class Data<T> {
    private Boolean status;
    private String message;
    private T data;

    public Data() {
    }

    public Data(Boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
