package threatgrid.kafka.connect.elasticsearch;

public class SslContextException extends RuntimeException {
    public SslContextException(Exception e) {
        super(e);
    }
}
