# HOW TO USE IT
### 1.create an interface and extend IEngine<Pojo> Pojo is a model class
``` java
public interface IAccountDao extends IEngine<Account>{
}
```
### 2.create a normal class extend AbstractEngine<Pojo> and implements the interface in step 1
```java
public class AccountDaoImpl extends AbstractEngine<Account>  implements IAccountDao<Account>{

}
```
### 3.the normal class in step 2 can use CRUD API 
```java
IAccountDao iAccountDao = new AccountDaoImpl();

Account account = new Account();
//create CRUD operation just like this:
boolean result = iAccountDao.create(account);
//query
List<Account> findList(Account account);
Account findById(Long id);
//delete
boolean detele(Long id);
//update
boolean updateById(Account);
```

## notice
the Pojo class need id field named "id" or Pojo's super class have this field