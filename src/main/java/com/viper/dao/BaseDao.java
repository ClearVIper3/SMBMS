package com.viper.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

public class BaseDao {

    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    //静态代码块，类加载的时候初始化
    static {
        //通过类加载器读取
        Properties properties = new Properties();
        InputStream is = BaseDao.class.getClassLoader().getResourceAsStream("db.properties");

        try {
            properties.load(is);
        } catch(IOException ie){
            ie.printStackTrace();
        }

        driver = properties.getProperty("driver");
        url = properties.getProperty("url");
        username = properties.getProperty("username");
        password = properties.getProperty("password");
    }

    public static Connection getConnection(){
        Connection conn = null;
        try{
            Class.forName(driver);
            conn = DriverManager.getConnection(url,username,password);
        } catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public static ResultSet execute(Connection conn, String sql, Object param[], ResultSet res, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement = conn.prepareStatement(sql);

        for(int i = 1; i <= param.length; i++){
            preparedStatement.setObject(i,param[i - 1]);
        }

        res = preparedStatement.executeQuery();
        return res;
    }

    public static int execute(Connection conn, String sql, Object param[],PreparedStatement preparedStatement) throws SQLException {
        preparedStatement = conn.prepareStatement(sql);

        for(int i = 1; i <= param.length; i++){
            preparedStatement.setObject(i,param[i - 1]);
        }

        int UpdateRows = preparedStatement.executeUpdate();
        return UpdateRows;
    }

    public static boolean closeResource(Connection conn,PreparedStatement preparedStatement,ResultSet rs){
        Boolean result = true;

        if(rs != null){
            try{
                rs.close();
                rs = null;
            } catch(SQLException ie){
                ie.printStackTrace();
                result = false;
            }
        }

        if(preparedStatement != null){
            try{
                preparedStatement.close();
                preparedStatement = null;
            } catch(SQLException ie){
                ie.printStackTrace();
                result = false;
            }
        }

        if(conn != null){
            try{
                conn.close();
                conn = null;
            } catch(SQLException ie){
                ie.printStackTrace();
                result = false;
            }
        }

        return result;
    }
}
