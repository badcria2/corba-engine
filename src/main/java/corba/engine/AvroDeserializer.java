package corba.engine;

import corba.engine.models.KafkaData;
import corba.engine.models.Tags;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvroDeserializer {

    // Método para deserializar una lista de objetos KafkaData desde un arreglo de bytes AVRO
    public static List<KafkaData> deserializeAvroList(byte[] avroData, String schemaString) throws IOException {
        // Parsear el esquema AVRO
        Schema schema = new Schema.Parser().parse(schemaString);

        // Crear un GenericDatumReader para leer los registros
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

        // Crear un Decoder a partir del arreglo de bytes AVRO
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(avroData);
        Decoder decoder = DecoderFactory.get().binaryDecoder(byteArrayInputStream, null);

        // Lista para almacenar los registros deserializados
        List<KafkaData> kafkaDataList = new ArrayList<>();

        try {
            // Leer registros hasta que no haya más datos
            while (true) {
                // Leer un registro
                GenericRecord record = reader.read(null, decoder);

                // Mapear el GenericRecord a KafkaData
                KafkaData kafkaData = mapGenericRecordToKafkaData(record);
                kafkaDataList.add(kafkaData);
            }
        } catch (org.apache.avro.AvroRuntimeException e) {
            // Manejar el final de los registros (cuando no hay más registros que leer)
            System.out.println("Fin de los registros AVRO.");
        }

        return kafkaDataList;
    }

    // Método para mapear un GenericRecord a un objeto KafkaData
    private static KafkaData mapGenericRecordToKafkaData(GenericRecord record) {
        KafkaData kafkaData = new KafkaData();

        // Mapeo del valor "name"
        kafkaData.setName(record.get("name").toString());

        // Mapeo del valor "timestamp"
        kafkaData.setTimestamp((Long) record.get("timestamp"));

        // Mapeo del valor "tags"
        GenericRecord tagsRecord = (GenericRecord) record.get("tags");
        Tags tags = new Tags();
        tags.setComponentName(tagsRecord.get("component_name").toString());
        tags.setSource(tagsRecord.get("source").toString());
        tags.setSubscriptionName(tagsRecord.get("subscription-name").toString());
        kafkaData.setTags(tags);

        // Mapeo de "values" (se asume que es un mapa de String a String)
        Map<String, String> values = (Map<String, String>) record.get("values");
        kafkaData.setValues(values);

        return kafkaData;
    }
}
