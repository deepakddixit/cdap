package com.continuuity.passport.dal.db;

import com.continuuity.common.db.DBConnectionPoolManager;
import com.continuuity.passport.common.sql.SQLChain;
import com.continuuity.passport.common.sql.SQLChainImpl;
import com.continuuity.passport.core.exceptions.ConfigurationException;
import com.continuuity.passport.core.meta.Account;
import com.continuuity.passport.core.meta.AccountSecurity;
import com.continuuity.passport.core.meta.BillingInfo;
import com.continuuity.passport.core.meta.Role;
import com.continuuity.passport.dal.AccountDAO;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * AccountDAO implementation that uses database as the persistence store
 */
public class AccountDBAccess implements AccountDAO {

  private Map<String, String> configuration;

  private DBConnectionPoolManager poolManager = null;

  /**
   * Create Account in the system
   * @param account Instance of {@code Account}
   * @return boolean status of account creation
   * @throws {@code RetryException}
   */
  @Override
  public long createAccount(Account account) throws ConfigurationException, RuntimeException {
    //TODO: Return boolean?
    if (this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection= this.poolManager.getConnection();

      PreparedStatement ps = null;
      String SQL = String.format( "INSERT INTO %s (%s,%s,%s,%s,%s) VALUES (?,?,?,?,?)", DBUtils.AccountTable.TABLE_NAME,
                                  DBUtils.AccountTable.EMAIL_COLUMN,
                                  DBUtils.AccountTable.FIRST_NAME_COLUMN, DBUtils.AccountTable.LAST_NAME_COLUMN,
                                  DBUtils.AccountTable.COMPANY_COLUMN, DBUtils.AccountTable.CONFIRMED_COLUMN);

      ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, account.getEmailId());
      ps.setString(2,account.getFirstName());
      ps.setString(3,account.getLastName());
      ps.setString(4,account.getCompany());
      ps.setInt(5, 0);

      ps.executeUpdate();
      ResultSet result = ps.getGeneratedKeys();
      if (result == null) {
        throw new RuntimeException("Failed Insert");
      }
      result.next();
      long id = result.getLong(1);

      return id;
    } catch (SQLException e) {
      //TODO: Log
      throw new RuntimeException(e.getMessage(), e.getCause());
    }
  }


  public boolean confirmRegistration(AccountSecurity security) throws ConfigurationException, RuntimeException{

    if (this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection = this.poolManager.getConnection();
      SQLChain chain = SQLChainImpl.getSqlChain(connection);
      //TODO: Update count should be 1
      chain.update(DBUtils.AccountTable.TABLE_NAME)
           .set(DBUtils.AccountTable.PASSWORD_COLUMN, generateSaltedHashedPassword(security.getPassword()))
           .set(DBUtils.AccountTable.CONFIRMED_COLUMN, DBUtils.AccountTable.ACCOUNT_CONFIRMED)
           .setLast(DBUtils.AccountTable.API_KEY_COLUMN, generateAPIKey())
           .where(DBUtils.AccountTable.ID_COLUMN).equal(security.getAccountId()).execute();
    }
    catch (SQLException e){
      throw new RuntimeException(e.getMessage(),e.getCause());
    }

    return true;
  }

  /**
   * Delete Account in the system
   *
   * @param accountId AccountId to be deleted
   * @return boolean status of account deletion
   * @throws {@code RetryException}
   */
  @Override
  public boolean deleteAccount(String accountId) throws ConfigurationException, RuntimeException {

    if(this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection = this.poolManager.getConnection();
      SQLChain chain = SQLChainImpl.getSqlChain(connection);
      chain.delete(DBUtils.AccountTable.TABLE_NAME)
           .where(DBUtils.AccountTable.EMAIL_COLUMN).equal(accountId).execute();

    }
    catch (SQLException e){
      throw new RuntimeException(e.getMessage(),e.getCause());
    }
    return true;
  }

  /**
   * GetAccount
   *
   * @param accountId id of the account
   * @return null if no entry matches, Instance of {@code Account} otherwise
   * @throws {@code RetryException}
   */
  @Override
  public Account getAccount(int accountId) throws ConfigurationException, RuntimeException {

    Account account = null;

    if(this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection = this.poolManager.getConnection();
      SQLChain chain = SQLChainImpl.getSqlChain(connection);

      List<Map<String,Object>> resultSet = chain.select(DBUtils.AccountTable.TABLE_NAME)
                                                .include(DBUtils.AccountTable.ID_COLUMN,
                                                         DBUtils.AccountTable.EMAIL_COLUMN,
                                                         DBUtils.AccountTable.FIRST_NAME_COLUMN,
                                                         DBUtils.AccountTable.LAST_NAME_COLUMN,
                                                         DBUtils.AccountTable.COMPANY_COLUMN)
                                                .where(DBUtils.AccountTable.ID_COLUMN).equal(accountId)
                                                .execute();


       if (resultSet.size() == 1 ) {


         Map<String,Object> dataSet = resultSet.get(0);
         account = new Account((String)dataSet.get(DBUtils.AccountTable.FIRST_NAME_COLUMN.toLowerCase()),
                               (String)dataSet.get(DBUtils.AccountTable.LAST_NAME_COLUMN.toLowerCase()),
                               (String)dataSet.get(DBUtils.AccountTable.COMPANY_COLUMN.toLowerCase()),
                               (String)dataSet.get(DBUtils.AccountTable.EMAIL_COLUMN.toLowerCase()),
                               (Integer)dataSet.get(DBUtils.AccountTable.ID_COLUMN.toLowerCase()));

       }


    }
    catch (SQLException e) {
      throw new RuntimeException(e.getMessage(),e.getCause());
    }
    return account;
  }


  @Override
  public boolean updateBillingInfo(int accountId, BillingInfo billingInfo) throws ConfigurationException,RuntimeException {
    if(this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection = this.poolManager.getConnection();
      SQLChain chain = SQLChainImpl.getSqlChain(connection);
      chain.insert(DBUtils.AccountPayment.TABLE_NAME)
           .columns(DBUtils.AccountPayment.ACCOUNT_ID_COLUMN, DBUtils.AccountPayment.CREDIT_CARD_NAME_COLUMN,
                    DBUtils.AccountPayment.CREDIT_CARD_NUMBER_COLUMN, DBUtils.AccountPayment.CREDIT_CARD_CVV_COLUMN,
                    DBUtils.AccountPayment.CREDIT_CARD_EXPIRY_COLUMN)
           .values(accountId,billingInfo.getCreditCardName(),billingInfo.getCreditCardNumber(),
                   billingInfo.getCvv(),billingInfo.getExpirationDate())
           .execute();
    }
    catch (SQLException e){
      throw new RuntimeException(e.getMessage(),e.getCause());
    }

    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }


  /**
   * Configure the Data access objects. Creates a connection pool manager
   * @param configuration Key value params for configuring the DAO
   */
  @Override
  public void configure(Map<String, String> configuration) {

    this.configuration = configuration;
    String connectionString = configuration.get("connectionString");

    String jdbcType = configuration.get("jdbcType");

    if (jdbcType.toLowerCase().equals("mysql")) {

      MysqlConnectionPoolDataSource mysqlDataSource =  new MysqlConnectionPoolDataSource();
      mysqlDataSource.setUrl(connectionString);


      this.poolManager = new DBConnectionPoolManager(mysqlDataSource, 20);

    }
  }

  @Override
  public boolean addRoleType(int accountId, Role role) throws ConfigurationException, RuntimeException {
    if(this.poolManager == null){
      throw new ConfigurationException("DBConnection pool is null. DAO is not configured");
    }
    try {
      Connection connection = this.poolManager.getConnection();

      SQLChain chain = SQLChainImpl.getSqlChain(connection);
      chain.insert(DBUtils.AccountRoleType.TABLE_NAME)
           .columns(DBUtils.AccountRoleType.ACCOUNT_ID_COLUMN, DBUtils.AccountRoleType.ROLE_NAME_COLUMN,
                    DBUtils.AccountRoleType.PERMISSIONS_COLUMN)
           .values(accountId, role.getRoleName(),role.getPermissions())
           .execute();
    }
    catch (SQLException e) {
      throw new RuntimeException(e.getMessage(),e.getCause());
    }
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  private String generateAPIKey(){
    //TODO: Generate API_KEY
    return "API_KEY";
  }

  private String generateSaltedHashedPassword(String password) {
    //TODO: Add this
    return password;

  }

}
