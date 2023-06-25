package requests;

import java.io.Serial;

public class AddRequest extends Request{

    @Serial
    private static final long serialVersionUID = 1L;
    public final String[] movieArguments;

    public AddRequest(String[] movieArguments) {
        super("add");
        this.movieArguments = movieArguments;
    }
}


