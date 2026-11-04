package com.essenza.aws.modules.amazonSQS.domain.dto.queues;

/**
 * Límites de Amazon SQS usados en las validaciones de DTOs y servicios.
 */
public final class SqsQueueConstraints {

    /** Nombre de cola: alfanumérico, guiones y guiones bajos, máx. 80 caracteres (incluido el sufijo .fifo). */
    public static final String QUEUE_NAME_PATTERN = "^(?=.{1,80}$)[A-Za-z0-9_-]+(\\.fifo)?$";

    /** URL de cola SQS: https://sqs.&lt;region&gt;.amazonaws.com/&lt;account&gt;/&lt;name&gt; (también el endpoint legado). */
    public static final String QUEUE_URL_PATTERN =
            "^https://(sqs\\.[a-z0-9-]+\\.amazonaws\\.com(\\.cn)?|queue\\.amazonaws\\.com)/\\d{12}/[A-Za-z0-9_-]{1,80}(\\.fifo)?$";

    public static final String FIFO_SUFFIX = ".fifo";

    public static final int MAX_DELAY_SECONDS = 900;
    public static final int MIN_MESSAGE_SIZE = 1024;
    public static final int MAX_MESSAGE_SIZE = 1_048_576;
    public static final int MIN_RETENTION_PERIOD = 60;
    public static final int MAX_RETENTION_PERIOD = 1_209_600;
    public static final int MAX_VISIBILITY_TIMEOUT = 43_200;
    public static final int MAX_WAIT_TIME_SECONDS = 20;
    public static final int MAX_RECEIVE_MESSAGES = 10;

    private SqsQueueConstraints() {
    }

    public static boolean isFifo(String nameOrUrl) {
        return nameOrUrl != null && nameOrUrl.endsWith(FIFO_SUFFIX);
    }
}
