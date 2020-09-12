package DBControllers;

import Models.User;

import java.net.URISyntaxException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DbController
{
    public static Connection getConnection() throws URISyntaxException, SQLException
    {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    public static void insertPlan(String tgId, String plan) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        try {
            dbConnection = getConnection();
            String uid = selectUser(tgId);
            statement = dbConnection.createStatement();

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            String insertPlan = "INSERT INTO public.\"History\" (\"userId\",\"planText\",\"loadDate\")" +
                    "VALUES ("+uid+", '"+plan+"', '"+date+"')";

            statement.execute(insertPlan);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public static Boolean isUserInDb(String tgId) throws SQLException {
        Connection dbCon = null;
        Statement statement = null;
        ResultSet rs = null;
        Boolean isIn = null;

        String query = "SELECT * FROM public.\"Users\" WHERE \"Users\".\"tgId\" = '"+tgId+"'";

        try {
            dbCon = getConnection();
            statement = dbCon.createStatement();
            rs = statement.executeQuery(query);

            if( !rs.next()){isIn=false; }
            else{isIn=true; }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {

            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (dbCon != null) {
                dbCon.close();
            }


    }
        System.out.println(isIn);
        return isIn;
    }

    public static void rewritePlan(String tgId, String plan) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        try {
            dbConnection = getConnection();
            String uid = selectUser(tgId);
            statement = dbConnection.createStatement();

            String insertPlan = "UPDATE public.\"History\" SET \"planText\"='"+plan+"' " +
                    "WHERE \"id\" IN (SELECT \"id\" FROM public.\"History\" ORDER BY \"History\".\"userId\"="+uid+" DESC, id DESC LIMIT 1)";

            statement.execute(insertPlan);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }
    public static void updatePlan(String tgId, String plan) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        try {
            dbConnection = getConnection();
            String uid = selectUser(tgId);
            statement = dbConnection.createStatement();

            String insertPlan = "UPDATE public.\"History\" SET \"planText\"=\"planText\"||'"+plan+"' " +
                    "WHERE \"id\" IN (SELECT \"id\" FROM public.\"History\" ORDER BY \"History\".\"userId\"="+uid+" DESC, id DESC LIMIT 1)";

            statement.execute(insertPlan);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public static void addUserToDb(String tgId, String name) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        try {
            dbConnection = getConnection();
            statement = dbConnection.createStatement();

            String insertUser = "INSERT INTO public.\"Users\" (\"tgId\",\"name\",\"isCurrentMember\")" +
                    "VALUES ('"+tgId+"', '"+name+"', "+true+")";

            statement.execute(insertUser);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    private static String selectUser(String tgId ) throws SQLException {
        Connection con = null;
        Statement statement = null;
        ResultSet rs = null;

        String query = "SELECT * FROM public.\"Users\" WHERE \"Users\".\"tgId\" = '"+tgId+"'";
        String userid = "%%%";
        try {
            con = getConnection();
            statement = con.createStatement();
             rs = statement.executeQuery(query);


             rs.next();
             userid = rs.getString("id");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (con != null) {
                con.close();
            }
        return userid;

    }
    }
    public static ArrayList<User> getAllUsersList() throws SQLException {
        ArrayList<User> users = new ArrayList<User>();

        Connection con = null;
        Statement statement = null;
        ResultSet rs = null;

        String query = "SELECT * FROM public.\"Users\" WHERE \"isCurrentMember\" = TRUE";
        try
        {
            con = getConnection();
            statement = con.createStatement();
            rs = statement.executeQuery(query);

            while(rs.next())
            {
                User u = new User();
                u.setUserTgId(rs.getString("tgId"));
                System.out.println(rs.getString("tgId"));
                u.setUsername(rs.getString("name"));
                System.out.println(rs.getString("name"));
                u.setReady(false);
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (con != null) {
                con.close();
            }

    }
        return users;
    }
    public static  String selectHistory(String tgId) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        ResultSet rs = null;
        String resHistory = "";

        try {
            dbConnection = getConnection();
            String uid = selectUser(tgId);

            String query = "SELECT * FROM public.\"History\" WHERE \"History\".\"userId\" = " + uid ;

            statement = dbConnection.createStatement();
            rs = statement.executeQuery(query);

            while (rs.next())
            {
                resHistory+=rs.getString("loadDate")+
                "\n##########\n"+
                rs.getString("planText")+"\n\n";
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            resHistory="Что то пошло не так";
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            resHistory="Что то пошло не так";
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return resHistory;
    }
    public static void unregUser(String tgId) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        try {
            dbConnection = getConnection();
            String uid = selectUser(tgId);
            statement = dbConnection.createStatement();

            System.out.println(uid);
            String insertPlan = "UPDATE public.\"Users\" SET \"isCurrentMember\"= FALSE WHERE \"id\" ="+uid;

            statement.execute(insertPlan);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

}
