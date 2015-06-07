package tours_app_server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility for converting ResultSets into different output formats
 * Original code can be found at Andrew Zakordonets's Gist:
 * https://gist.github.com/azakordonets/11040771
 * The code below is a modified version with added functionality.
 */
public class ResultSetConverter {

    /**
     * Convert a result set into a JSON Array
     * @param  resultSet	 the <code>ResultSet</code> to convert to JSON format
     * @return JSONArray     An array whose elements are the rows of the <code>ResultSet</code>.
     * 						 Each element contains columns with their respected values.
     * @throws SQLException  if a database access error occurs    
     * @throws JSONException if the value is non-finite number or if the key is null 
     */
    public static JSONArray convertResultSetIntoJSON(ResultSet resultSet) throws SQLException, JSONException {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            int totalColumns = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < totalColumns; i++) {
                String columnName = resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase();
                Object columnValue = resultSet.getObject(i + 1);
                // if the value in the DB is null, then set it to the default value
                if (columnValue == null) {
                    columnValue = "null";
                }
                obj.put(columnName, columnValue);
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }
    
    /**
     * Obtain a single boolean result from result set.
     * 
     * @param resultSet					the <code>ResultSet</code> from which to obtain the data.
     * 									resultSet must include a single result (one row and one column)
     * @return							A <code>boolean</code> value contained in resultSet. 
     * @throws SQLException 			if a database access error occurs or this method is called on a closed resultSet
     * @throws IllegalArgumentException if resultSet contains a value which is not a <code>boolean</code>
     */
    public static boolean convertResultSetIntoBoolean(ResultSet resultSet) throws SQLException {
    	final int FIRST_COLUMN = 1;
    	
    	resultSet.next();
    	if (resultSet.getMetaData().getColumnType(FIRST_COLUMN) == java.sql.Types.BOOLEAN) {
    		return resultSet.getBoolean(FIRST_COLUMN);
    	}
    	else {
    		throw new IllegalArgumentException("IllegalArgumentExeception: value in ResultSet is not a boolean"); 
    	}
    }
    
    /**
     * Obtain a single String result from result set.
     * 
     * @param resultSet					the <code>ResultSet</code> from which to obtain the data.
     * 									resultSet must include a single result (one row and one column)
     * @return							A <code>String</code> value contained in resultSet. 
     * @throws SQLException 			if a database access error occurs or this method is called on a closed resultSet
     * @throws IllegalArgumentException if resultSet contains a value which is not a <code>String</code>
     */
    public static String convertResultSetIntoString(ResultSet resultSet) throws SQLException {
    	final int FIRST_COLUMN = 1;
    	
    	resultSet.next();
    	if (resultSet.getMetaData().getColumnType(FIRST_COLUMN) == java.sql.Types.VARCHAR) {
    		return resultSet.getString(FIRST_COLUMN);
    	}
    	else {
    		throw new IllegalArgumentException("IllegalArgumentExeception: value in ResultSet is not a String"); 
    	}
    }
    
    /**
     * Obtain a single integer result from result set.
     * 
     * @param resultSet					the <code>ResultSet</code> from which to obtain the data.
     * 									resultSet must include a single result (one row and one column)
     * @return							An <code>int</code> value contained in resultSet. 
     * @throws SQLException 			if a database access error occurs or this method is called on a closed resultSet
     * @throws IllegalArgumentException if resultSet contains a value which is not an <code>int</code>
     */
    public static int convertResultSetIntoInt(ResultSet resultSet) throws SQLException {
    	final int FIRST_COLUMN = 1;
    	
    	resultSet.next();
    	if (resultSet.getMetaData().getColumnType(FIRST_COLUMN) == java.sql.Types.INTEGER) {
    		return resultSet.getInt(FIRST_COLUMN);
    	}
    	else {
    		throw new IllegalArgumentException("IllegalArgumentExeception: value in ResultSet is not an integer"); 
    	}
    }

    public static int converBooleanIntoInt(boolean bool) {
        if (bool) return 1;
        else return 0;
    }

    public static int convertBooleanStringIntoInt(String bool) {
        if (bool.equals("false")) return 0;
        else if (bool.equals("true")) return 1;
        else {
            throw new IllegalArgumentException("Wrong value is passed to the method. Value is " + bool);
        }
    }

    public static double getDoubleOutOfString(String value, String format, Locale locale) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(locale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat f = new DecimalFormat(format,otherSymbols);
        String formattedValue = f.format(Double.parseDouble(value));
        double number = Double.parseDouble(formattedValue);
        return Math.round(number * 100.0) / 100.0;
    }

}
