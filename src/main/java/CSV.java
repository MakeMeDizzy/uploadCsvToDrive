import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSV {

    public String export(String table, String query) throws SQLException, ClassNotFoundException, IOException {
        String csvFileName = getFileName(table.concat("_Export"));

        try (Connection connection = HikariCPDataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)
        ){

            ResultSetMetaData metaData = result.getMetaData();
            int numberOfColumns = metaData.getColumnCount();
            String headerLine = "";

            for (int i = 1; i <= numberOfColumns; i++){
                String columnName = metaData.getColumnName(i);
                headerLine = headerLine.concat(columnName).concat(",");
            }


            try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(csvFileName))) {

                fileWriter.write(headerLine.substring(0, headerLine.length() - 1));

                while (result.next()) {
                    String line = "";

                    for (int i = 1; i <= numberOfColumns; i++) {

                        line = line.concat(String.valueOf(result.getObject(i)));

                        if (i != numberOfColumns) {
                            line = line.concat(",");
                        }
                    }

                    fileWriter.newLine();
                    fileWriter.write(line);
                }

            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return csvFileName;
    }



    private String getFileName(String baseName){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTimeInfo = dateFormat.format(new Date());
        return baseName.concat(String.format("_%s.csv", dateTimeInfo));
    }

}
