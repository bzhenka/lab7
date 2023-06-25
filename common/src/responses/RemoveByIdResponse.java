package responses;

import java.io.Serial;

public class RemoveByIdResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    public final Boolean removed;

    public RemoveByIdResponse(String error, Boolean removed) {
        super("removeById", error);
        this.removed = removed;
    }
}
