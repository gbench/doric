-- --------------------------
-- # getDatabase
-- --------------------------
select database()

-- --------------------------
-- # getUserByName
-- #name: 用户名称, #表示替换的时候加引号
-- ##max: 最高, ##表示值替换的时候不带引号
-- ##min: 最小, ##表示值替换的时候不带引号
-- --------------------------
select * from t_user u where u.name=#name and u.height between ##max and ##min