package io.pivotal.shinyay.batch.configuration.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
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

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        String minSQL = "SELECT MIN(:column) from :table";
        String maxSQL = "SELECT MAX(:column) from :table";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("column", column)
                .addValue("table", table);

        Integer min = jdbcTemplate.queryForObject(minSQL, sqlParameterSource, Integer.class);
        Integer max = jdbcTemplate.queryForObject(maxSQL, sqlParameterSource, Integer.class);

        int targetSize = (max - min) / gridSize + 1;

        return null;
    }
}
