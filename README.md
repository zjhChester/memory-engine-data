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

---
## 2.0 add API about findList 
use QueryWrapper to select List by conditions:   
```java
List<T> findList(QueryWrapper queryWrapper);
```
parameters list in QueryWrapper's methods
```java
QueryWrapper equals(String fieldName,Object fieldValue);
QueryWrapper lessThan(String fieldName,Object fieldValue);
QueryWrapper moreThan(String fieldName,Object fieldValue);
QueryWrapper like(String fieldName,Object fieldValue);
```
example:
```java
QueryWrapper queryWrapper = new QueryWrapper();
queryWrapper = queryWrapper.equals("fieldName",ObjectValue);
new DaoImpl().findList(queryWrapper);
```
use chain call to append your conditions:
```java
QueryWrapper queryWrapper = new QueryWrapper();
queryWrapper = queryWrapper.equals("note","likeNote")
        .lessThan("age",19)
        .moreThan("age",15)
        .like("name","jiahao");
```