
-- --------------------------
-- # getCytokines
-- #name: 细胞名称
-- --------------------------
match (n)-[:Secrete]->(b) where n.name=#name return n.name as host,b.name as name,b.functions as description

-- --------------------------
-- # getCells
-- #name: 细胞因子名称
-- --------------------------
match (n)-[:Secrete]->(b) where b.name=#name return n.name as host

-- --------------------------
-- # setVersion
-- #version: 节点版本
-- --------------------------
match (n)-[:Secrete]->(b) set n.version=##version

-- --------------------------
-- # createLine
-- #version: 节点版本
-- --------------------------
血压下降|钠量减少-[刺激>肾的球旁细胞>分泌]->肾素-[作用>血管紧张素原>生成]->血管紧张素-[刺激>肾上腺皮质球状带>合成和分泌]->
醛固酮-[作用>远曲小管|集合管 上皮细胞>重吸收]->水|钠;
1|2|3|4|5-[>>匹配]->a|b|c|d