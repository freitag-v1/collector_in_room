LOCK TABLES `bounding_box_table` WRITE;
DELETE FROM bounding_box_table;
UNLOCK TABLES;

LOCK TABLES `candidate_table` WRITE;
DELETE FROM candidate_table;
UNLOCK TABLES;

LOCK TABLES `class_table` WRITE;
DELETE FROM class_table;
INSERT INTO `class_table` VALUES (153,1,'고양이'),(154,1,'까치'),(155,1,'강아지'),(156,2,'까치'),(157,2,'강아지'),(158,2,'고양이');
UNLOCK TABLES;

LOCK TABLES `collection_work_history` WRITE;
DELETE FROM collection_work_history;
UNLOCK TABLES;

LOCK TABLES `labelling_work_history` WRITE;
DELETE FROM labelling_work_history;
INSERT INTO `labelling_work_history` VALUES (58,1,'worker','boundingBox',11,12,13,14,15);
UNLOCK TABLES;

LOCK TABLES `problem_table` WRITE;
DELETE FROM problem_table;
INSERT INTO `problem_table` VALUES (2547,1,1,-1,'requester07012307','','','','작업전','','',0),(2548,2,1,-1,'requester07012307','','','','작업전','','',0),(2549,3,1,-1,'requester07012307','','','','작업전','','',0),(2550,4,1,-1,'requester07012307','','','','작업전','','',0),(2551,5,1,-1,'requester07012307','','','','작업전','','',0),(2552,6,1,-1,'requester07012307','','','','작업전','','',0),(2553,7,1,-1,'requester07012307','','','','작업전','','',0),(2554,8,1,-1,'requester07012307','','','','작업전','','',0),(2555,9,1,-1,'requester07012307','','','','작업전','','',0),(2556,10,1,-1,'requester07012307','','','','작업전','','',0),(2557,11,2,-1,'requester07012310','Cat03.jpg','','','작업중','','하',0),(2558,12,2,-1,'requester07012310','Chinook-On-White-03.jpg','','','작업중','','하',0),(2559,13,2,-1,'requester07012310','HUX525WGXFH5HO2EISAYTERY5I.jpg','','','작업중','','하',0),(2560,14,2,-1,'requester07012310','Magpies-magpie-perches-log.jpg','','','작업중','','하',0),(2561,15,2,-1,'requester07012310','apa_2016-p1_1428_1_black-billed-magpie_amanda_ubell_kk.jpg','','','작업중','','하',0),(2562,16,2,-1,'requester07012310','black-billed-magpie-5a66065de258f800378ace19.jpg','','','작업전','','',0),(2563,17,2,-1,'requester07012310','close-up-of-cat-wearing-sunglasses-while-sitting-royalty-free-image-1571755145.jpg','','','작업전','','',0),(2564,18,2,-1,'requester07012310','dd98d9d98e3b3c8bdc91997ca957f5a6.jpg','','','작업전','','',0),(2565,19,2,-1,'requester07012310','dog-puppy-on-garden-royalty-free-image-1586966191.jpg','','','작업전','','',0),(2566,20,2,-1,'requester07012310','dogs_1280p_0.jpg','','','작업전','','',0),(2567,21,2,-1,'requester07012310','download.jpeg','','','작업전','','',0),(2568,22,2,-1,'requester07012310','emperor_penguin_shutterstock_1024.jpg','','','작업전','','',0),(2569,23,2,-1,'requester07012310','h_yellow-billed-magpie_004_winter_california_aaronmaizlish_flickrcc-by-nc-2.0_adult.jpg','','','작업전','','',0),(2570,24,2,-1,'requester07012310','magpie_david_chapman_768.jpg','','','작업전','','',0),(2571,25,2,-1,'requester07012310','maxresdefault.jpg','','','작업전','','',0);
UNLOCK TABLES;

LOCK TABLES `project_table` WRITE;
DELETE FROM project_table;
INSERT INTO `project_table` VALUES (337,1,'requester','동물 사진 수집','freitag-test','진행중','collection','image','동물',0,'잘','잘','_111515733_gettyimages-1208779325.jpg','동물 사진 수집',10,0,0,500),(338,2,'requester','동물 바운딩 박스','freitag-test','진행중','labelling','boundingBox','동물',0,'2','3','_111515733_gettyimages-1208779325.jpg','1',15,0,0,750);
UNLOCK TABLES;

LOCK TABLES `user_table` WRITE;
DELETE FROM user_table;
INSERT INTO `user_table` VALUES (28,'requester','$2a$10$pRTO72eGZFlXM.edcZNVOO0UpfjH0j37vMgK0vzRRv33tiITYofhW','의뢰자','01027540421','wodnd999999@ajou.ac.kr','Ajou Univ.',30,'2020-06-25 00:00:00',100000,98750,'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ',1100758491,0),(29,'worker','$2a$10$pRTO72eGZFlXM.edcZNVOO0UpfjH0j37vMgK0vzRRv33tiITYofhW','작업자','01027540421','wodnd999999@ajou.ac.kr','Ajou Univ.',30,'2020-06-25 00:00:00',200000,200000,'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ',1100758491,0);
UNLOCK TABLES;