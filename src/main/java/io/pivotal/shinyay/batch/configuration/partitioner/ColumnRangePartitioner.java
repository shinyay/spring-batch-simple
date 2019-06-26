package io.pivotal.shinyay.batch.configuration.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {

    private String column;
    private String table;
    private DataSource dataSource;

    public ColumnRangePartitioner(String column, String table, DataSource dataSource) {
        this.column = column;
        this.table = table;
        this.dataSource = dataSource;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        System.out.println(">>> partition");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String minSQL = "SELECT MIN(id) from customer";
        String maxSQL = "SELECT MAX(id) from customer";

        Integer min = jdbcTemplate.queryForObject(minSQL, Integer.class);
        Integer max = jdbcTemplate.queryForObject(maxSQL, Integer.class);

        int targetSize = (max - min) / gridSize + 1;

        HashMap<String, ExecutionContext> result = new HashMap<>();
        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while(start <= max) {
            ExecutionContext context = new ExecutionContext();
            result.put("PARTITION:" + number, context);

            context.putInt("minValue", start);
            context.putInt("maxValue", end);

            if(end >= max) {
                end = max;
            }

            start += targetSize;
            end += targetSize;
            number++;
        }
        return result;
    }
}
