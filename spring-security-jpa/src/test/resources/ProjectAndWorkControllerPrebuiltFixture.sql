LOCK TABLES bounding_box_table WRITE;
DELETE FROM bounding_box_table;
INSERT INTO bounding_box_table VALUES (68,1,27,'도로 간판','0.58 0.15 0.85 0.31'),(69,2,27,'도로 간판','0.40 0.44 0.54 0.55'),(70,3,27,'도로 간판','0.83 0.41 0.88 0.56'),(71,4,27,'도로 간판','0.71 0.12 0.85 0.18'),(72,5,27,'신호등','0.35 0.35 0.44 0.40'),(73,6,27,'신호등','0.62 0.42 0.71 0.46'),(74,7,28,'도로 간판','0.72 0.06 0.99 0.14'),(75,8,28,'신호등','0.14 0.13 0.24 0.19'),(76,9,28,'신호등','0.58 0.15 0.67 0.19'),(77,10,28,'신호등','0.76 0.17 0.84 0.19'),(78,11,28,'신호등','0.85 0.31 1.00 0.32'),(79,12,29,'신호등','0.03 0.46 0.24 0.54'),(80,13,29,'신호등','0.39 0.46 0.59 0.53'),(81,14,29,'도로 간판','0.27 0.22 0.75 0.59'),(82,15,29,'도로 간판','0.64 0.63 0.83 0.71'),(83,16,30,'도로 간판','0.51 0.38 0.59 0.45'),(84,17,30,'도로 간판','0.81 0.45 0.89 0.50'),(85,18,30,'신호등','0.43 0.41 0.51 0.45'),(86,19,30,'신호등','0.60 0.42 0.68 0.46'),(87,20,30,'신호등','0.54 0.63 0.58 0.66'),(88,21,31,'신호등','0.17 0.46 0.38 0.59'),(89,22,31,'도로 간판','0.42 0.34 0.71 0.59');
UNLOCK TABLES;

LOCK TABLES candidate_table WRITE;
DELETE FROM candidate_table;
UNLOCK TABLES;

LOCK TABLES class_table WRITE;
DELETE FROM class_table;
INSERT INTO class_table VALUES (66,1,'얼굴 정면'),(67,1,'얼굴 옆면'),(69,2,'바다코끼리'),(70,2,'물개'),(71,2,'물범'),(72,2,'바다사자'),(73,2,'바다표범'),(74,3,'암사자'),(75,3,'숫사자'),(76,4,'도로 간판'),(77,4,'신호등'),(78,5,'어린이 보호구역'),(79,6,'홍로'),(80,6,'아오리'),(81,6,'홍옥'),(82,6,'부사'),(83,6,'추광');
UNLOCK TABLES;

LOCK TABLES collection_work_history WRITE;
DELETE FROM collection_work_history;
INSERT INTO collection_work_history VALUES (16,'middle_user1',1);
UNLOCK TABLES;

LOCK TABLES labelling_work_history WRITE;
DELETE FROM labelling_work_history;
INSERT INTO labelling_work_history VALUES (40,1,'high_user1','boundingBox',27,28,29,30,31),(41,2,'high_user2','classification',83,57,61,6,77),(42,3,'high_user2','classification',92,84,57,77,6),(43,4,'high_user2','classification',92,84,57,6,77),(46,7,'middle_user1','classification',119,112,85,78,7),(47,8,'middle_user2','classification',128,86,104,79,8),(55,9,'low_user1','classification',139,113,87,9,80);
UNLOCK TABLES;

LOCK TABLES problem_table WRITE;
DELETE FROM problem_table;
INSERT INTO problem_table VALUES (1012,1,1,-1,'admin06251652','마스크김서린사진.jpg','얼굴 정면','','작업후','middle_user1','중',0),(1013,2,1,-1,'admin06251652','','','','작업전','','',0),(1014,3,1,-1,'admin06251652','','','','작업전','','',0),(1015,4,1,-1,'admin06251652','','','','작업전','','',0),(1016,5,1,-1,'admin06251652','','','','작업전','','',0),(1017,6,2,-1,'admin06251702','물개1.jpeg','물개','물개','검증완료','high_user2','상',1),(1018,7,2,-1,'admin06251702','물개2.jpg','물개','','작업후','middle_user1','중',0),(1019,8,2,-1,'admin06251702','물범1.jpg','물범','','작업후','middle_user2','중',0),(1020,9,2,-1,'admin06251702','물범2.jpeg','물범','','작업후','low_user1','하',0),(1021,10,2,-1,'admin06251702','바다사자1.jpeg','','','작업전','','',0),(1022,11,2,-1,'admin06251702','바다사자2.jpeg','','','작업전','','',0),(1023,12,2,-1,'admin06251702','바다코끼리1.jpg','','','작업전','','',0),(1024,13,2,-1,'admin06251702','바다코끼리2.jpg','','','작업전','','',0),(1025,14,2,-1,'admin06251702','바다표범1.jpeg','','','작업전','','',0),(1026,15,2,-1,'admin06251702','바다표범2.jpeg','','','작업전','','',0),(1028,17,3,-1,'admin06251706','숫사자1.jpeg','숫사자','숫사자','검증완료','high_user1','',1),(1029,18,3,-1,'admin06251706','숫사자2.jpg','숫사자','숫사자','검증완료','high_user1','',1),(1030,19,3,-1,'admin06251706','숫사자3.jpeg','숫사자','숫사자','검증완료','high_user2','',1),(1031,20,3,-1,'admin06251706','숫사자4.jpeg','숫사자','숫사자','검증완료','high_user2','',1),(1032,21,3,-1,'admin06251706','숫사자5.jpg','숫사자','숫사자','검증완료','middle_user1','',1),(1033,22,3,-1,'admin06251706','암사자1.jpg','암사자','암사자','검증완료','middle_user1','',1),(1034,23,3,-1,'admin06251706','암사자2.jpg','암사자','암사자','검증완료','middle_user1','',1),(1035,24,3,-1,'admin06251706','암사자3.jpeg','암사자','암사자','검증완료','middle_user1','',1),(1036,25,3,-1,'admin06251706','암사자4.jpeg','암사자','암사자','검증완료','middle_user1','',0),(1037,26,3,-1,'admin06251706','암사자5.jpg','암사자','암사자','검증완료','','',1),(1038,27,4,-1,'admin06251711','도로이미지1.jpg','1 2 3 4 5 6 ','','작업후','high_user1','상',0),(1039,28,4,-1,'admin06251711','도로이미지10.jpg','7 8 9 10 11 ','','작업후','high_user1','상',0),(1040,29,4,-1,'admin06251711','도로이미지11.jpg','12 13 14 15 ','','작업후','high_user1','상',0),(1041,30,4,-1,'admin06251711','도로이미지12.jpeg','16 17 18 19 20 ','','작업후','high_user1','상',0),(1042,31,4,-1,'admin06251711','도로이미지13.jpg','21 22 ','','작업후','high_user1','상',0),(1043,32,4,-1,'admin06251711','도로이미지14.jpg','','','작업전','','',0),(1044,33,4,-1,'admin06251711','도로이미지15.jpg','','','작업전','','',0),(1045,34,4,-1,'admin06251711','도로이미지16.jpg','','','작업전','','',0),(1046,35,4,-1,'admin06251711','도로이미지17.jpg','','','작업전','','',0),(1047,36,4,-1,'admin06251711','도로이미지18.jpg','','','작업전','','',0),(1048,37,4,-1,'admin06251711','도로이미지19.jpeg','','','작업전','','',0),(1049,38,4,-1,'admin06251711','도로이미지2.jpg','','','작업전','','',0),(1050,39,4,-1,'admin06251711','도로이미지20.jpg','','','작업전','','',0),(1051,40,4,-1,'admin06251711','도로이미지3.jpg','','','작업전','','',0),(1052,41,4,-1,'admin06251711','도로이미지4.jpeg','','','작업전','','',0),(1053,42,4,-1,'admin06251711','도로이미지5.jpg','','','작업전','','',0),(1054,43,4,-1,'admin06251711','도로이미지6.jpg','','','작업전','','',0),(1055,44,4,-1,'admin06251711','도로이미지7.jpg','','','작업전','','',0),(1056,45,4,-1,'admin06251711','도로이미지8.jpg','','','작업전','','',0),(1057,46,4,-1,'admin06251711','도로이미지9.jpeg','','','작업전','','',0),(1058,47,5,-1,'admin06251715','1.jpg','','','작업전','','',0),(1059,48,5,-1,'admin06251715','10.jpg','','','작업전','','',0),(1060,49,5,-1,'admin06251715','2.png','','','작업전','','',0),(1061,50,5,-1,'admin06251715','3.png','','','작업전','','',0),(1062,51,5,-1,'admin06251715','4.jpg','','','작업전','','',0),(1063,52,5,-1,'admin06251715','5.jpg','','','작업전','','',0),(1064,53,5,-1,'admin06251715','6.jpg','','','작업전','','',0),(1065,54,5,-1,'admin06251715','7.jpg','','','작업전','','',0),(1066,55,5,-1,'admin06251715','8.jpg','','','작업전','','',0),(1067,56,5,-1,'admin06251715','9.jpeg','','','작업전','','',0),(1068,57,4,27,'admin06251711','도로이미지1.jpg','1 2 3 4 5 6','없음','교차검증후','high_user2','상',0),(1069,58,4,27,'admin06251711','도로이미지1.jpg','없음','없음','교차검증전',NULL,'중',0),(1070,59,4,27,'admin06251711','도로이미지1.jpg','없음','없음','교차검증전',NULL,'중',0),(1071,60,4,27,'admin06251711','도로이미지1.jpg','없음','없음','교차검증전',NULL,'하',0),(1072,61,4,28,'admin06251711','도로이미지10.jpg','없음','없음','교차검증전','','상',0),(1073,62,4,28,'admin06251711','도로이미지10.jpg','없음','없음','교차검증전',NULL,'중',0),(1074,63,4,28,'admin06251711','도로이미지10.jpg','없음','없음','교차검증전',NULL,'중',0),(1075,64,4,28,'admin06251711','도로이미지10.jpg','없음','없음','교차검증전',NULL,'하',0),(1076,65,4,29,'admin06251711','도로이미지11.jpg','없음','없음','교차검증전',NULL,'상',0),(1077,66,4,29,'admin06251711','도로이미지11.jpg','없음','없음','교차검증전',NULL,'중',0),(1078,67,4,29,'admin06251711','도로이미지11.jpg','없음','없음','교차검증전',NULL,'중',0),(1079,68,4,29,'admin06251711','도로이미지11.jpg','없음','없음','교차검증전',NULL,'하',0),(1080,69,4,30,'admin06251711','도로이미지12.jpeg','없음','없음','교차검증전',NULL,'상',0),(1081,70,4,30,'admin06251711','도로이미지12.jpeg','없음','없음','교차검증전',NULL,'중',0),(1082,71,4,30,'admin06251711','도로이미지12.jpeg','없음','없음','교차검증전',NULL,'중',0),(1083,72,4,30,'admin06251711','도로이미지12.jpeg','없음','없음','교차검증전',NULL,'하',0),(1084,73,4,31,'admin06251711','도로이미지13.jpg','없음','없음','교차검증전',NULL,'상',0),(1085,74,4,31,'admin06251711','도로이미지13.jpg','없음','없음','교차검증전',NULL,'중',0),(1086,75,4,31,'admin06251711','도로이미지13.jpg','없음','없음','교차검증전',NULL,'중',0),(1087,76,4,31,'admin06251711','도로이미지13.jpg','없음','없음','교차검증전',NULL,'하',0),(1088,77,6,-1,'admin06251804','부사.jpg','부사','','작업후','high_user2','상',0),(1089,78,6,-1,'admin06251804','아오리사과.png','아오리','','작업후','middle_user1','중',0),(1090,79,6,-1,'admin06251804','아오리사과속주지훈.jpg','아오리','','작업후','middle_user2','중',0),(1091,80,6,-1,'admin06251804','추광.jpg','홍옥','','작업후','low_user1','하',0),(1092,81,6,-1,'admin06251804','홍로.jpg','','','작업전','','',0),(1093,82,6,-1,'admin06251804','홍옥.jpeg','','','작업전','','',0),(1095,84,2,6,'admin06251702','물개1.jpeg','물개','물개','검증완료','high_user2','상',1),(1096,85,2,6,'admin06251702','물개1.jpeg','물개','물개','검증완료','middle_user1','중',1),(1097,86,2,6,'admin06251702','물개1.jpeg','물범','물개','검증완료','middle_user2','중',0),(1098,87,2,6,'admin06251702','물개1.jpeg','물개','물개','검증완료','low_user1','하',1),(1099,88,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'상',0),(1100,89,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'중',0),(1101,90,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'중',0),(1102,91,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'하',0),(1104,92,3,22,'admin06251706','암사자1.jpg','암사자','암사자','검증완료','high_user2',NULL,1),(1105,93,2,6,'admin06251702','물개1.jpeg','없음','없음','교차검증전','','상',0),(1106,94,2,6,'admin06251702','물개1.jpeg','없음','없음','교차검증전',NULL,'중',0),(1107,95,2,6,'admin06251702','물개1.jpeg','없음','없음','교차검증전',NULL,'중',0),(1108,96,2,6,'admin06251702','물개1.jpeg','없음','없음','교차검증전',NULL,'하',0),(1109,97,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'상',0),(1110,98,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'중',0),(1111,99,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'중',0),(1112,100,6,77,'admin06251804','부사.jpg','없음','없음','교차검증전',NULL,'하',0),(1114,102,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'상',0),(1115,103,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'상',0),(1116,104,2,7,'admin06251702','물개2.jpg','물개','없음','교차검증후','middle_user2','중',0),(1117,105,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'하',0),(1118,106,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'상',0),(1119,107,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'상',0),(1120,108,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'중',0),(1121,109,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'하',0),(1122,110,1,1,'admin06251652','마스크김서린사진.jpg','없음','없음','교차검증전',NULL,'상',0),(1123,111,1,1,'admin06251652','마스크김서린사진.jpg','없음','없음','교차검증전',NULL,'상',0),(1124,112,1,1,'admin06251652','마스크김서린사진.jpg','얼굴 옆면','없음','교차검증후','middle_user1','중',0),(1125,113,1,1,'admin06251652','마스크김서린사진.jpg','얼굴 정면','없음','교차검증후','low_user1','하',0),(1126,114,3,-1,'admin06251706','숫사자5.jpg','숫사자','숫사자','검증완료','middle_user2','',1),(1127,115,3,-1,'admin06251706','암사자1.jpg','암사자','암사자','검증완료','middle_user2','',1),(1128,116,3,-1,'admin06251706','암사자2.jpg','암사자','암사자','검증완료','middle_user2','',1),(1129,117,3,-1,'admin06251706','암사자3.jpeg','암사자','암사자','검증완료','middle_user2','',1),(1130,118,3,-1,'admin06251706','암사자4.jpeg','암사자','암사자','검증완료','middle_user2','',0),(1132,119,3,116,'admin06251706','암사자2.jpg','암사자','암사자','검증완료','middle_user1',NULL,1),(1133,120,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'상',0),(1134,121,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'상',0),(1135,122,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'중',0),(1136,123,2,7,'admin06251702','물개2.jpg','없음','없음','교차검증전',NULL,'하',0),(1137,124,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'상',0),(1138,125,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'상',0),(1139,126,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'중',0),(1140,127,6,78,'admin06251804','아오리사과.png','없음','없음','교차검증전',NULL,'하',0),(1141,128,3,115,'admin06251706','암사자1.jpg','암사자','암사자','검증완료','middle_user2',NULL,1),(1142,129,2,8,'admin06251702','물범1.jpg','없음','없음','교차검증전',NULL,'상',0),(1143,130,2,8,'admin06251702','물범1.jpg','없음','없음','교차검증전',NULL,'상',0),(1144,131,2,8,'admin06251702','물범1.jpg','없음','없음','교차검증전',NULL,'중',0),(1145,132,2,8,'admin06251702','물범1.jpg','없음','없음','교차검증전',NULL,'하',0),(1146,133,6,79,'admin06251804','아오리사과속주지훈.jpg','없음','없음','교차검증전',NULL,'상',0),(1147,134,6,79,'admin06251804','아오리사과속주지훈.jpg','없음','없음','교차검증전',NULL,'상',0),(1148,135,6,79,'admin06251804','아오리사과속주지훈.jpg','없음','없음','교차검증전',NULL,'중',0),(1149,136,6,79,'admin06251804','아오리사과속주지훈.jpg','없음','없음','교차검증전',NULL,'하',0),(1150,137,3,-1,'admin06251706','숫사자5.jpg','숫사자','숫사자','검증완료','low_user1','',0),(1151,138,3,-1,'admin06251706','암사자1.jpg','암사자','암사자','검증완료','low_user1','',1),(1938,139,3,119,'admin06251706','암사자2.jpg','암사자','암사자','검증완료','low_user1',NULL,1),(1939,140,2,9,'admin06251702','물범2.jpeg','없음','없음','교차검증전',NULL,'상',0),(1940,141,2,9,'admin06251702','물범2.jpeg','없음','없음','교차검증전',NULL,'상',0),(1941,142,2,9,'admin06251702','물범2.jpeg','없음','없음','교차검증전',NULL,'중',0),(1942,143,2,9,'admin06251702','물범2.jpeg','없음','없음','교차검증전',NULL,'중',0),(1943,144,6,80,'admin06251804','추광.jpg','없음','없음','교차검증전',NULL,'상',0),(1944,145,6,80,'admin06251804','추광.jpg','없음','없음','교차검증전',NULL,'상',0),(1945,146,6,80,'admin06251804','추광.jpg','없음','없음','교차검증전',NULL,'중',0),(1946,147,6,80,'admin06251804','추광.jpg','없음','없음','교차검증전',NULL,'중',0);
UNLOCK TABLES;

LOCK TABLES project_table WRITE;
DELETE FROM project_table;
INSERT INTO project_table VALUES (23,1,'admin','마스크 착용하고 안경 김 서린 사진 수집','admin06251652','진행중','collection','image','인물',0,'반드시 본인이 찍은 얼굴 사진이어야 합니다.\n얼굴 정면, 옆면 이렇게 두가지로 수집을 진행하면 됩니다.','단, 마스크와 안경을 필수적으로 착용해야합니다.\n또한 김이 서려야 되며, 이마부터 턱끝까지는 반드시 나와야 포인트가 지급이 됩니다. ','KakaoTalk_20200625_164249151.jpg','마스크를 낀 사람이 안경을 꼈을 때 김이 서려 얼굴 인식이 실패하는 경우가 많습니다.\n따라서 이러한 실패를 최소화하고자 마스크를 끼고 안경에 김이 서린 얼굴 사진을 수집하려고 합니다.',5,1,0,250),(24,2,'admin','바다에 사는 포유류 분류 작업','admin06251702','진행중','labelling','classification','동물',0,'바다에 사는 표유류를 주어진 라벨에 맞게 분류를 하면 됩니다.\n단, 중복 선택은 불가능 합니다. ','어려운 작업이라 생각하기 때문에 모르는 사진은 검색해서 분류하셔도 됩니다. ','예시데이터.jpg','바다에 사는 포유류를 주어진 라벨에 맞게 분류를 하면 됩니다. ',11,4,1,550),(25,3,'admin','암사자와 숫사자를 분류하는 작업','admin06251706','검증완료','labelling','classification','동물',0,'암사자와 숫사자를 주어진 사진에 맞게 분류를 하면 됩니다. ','암사자와 숫사자를 라벨에 맞게 분류를 진행하면 됩니다.\n화이팅!','사자예시.jpg','암사자와 숫사자를 구분하는 분류기를 제작하려고 합니다.\n주어진 사진을 라벨에 맞게 분류하시면 됩니다. ',10,10,10,550),(26,4,'admin','도로 이미지 바운딩 박스','admin06251711','진행중','labelling','boundingBox','도로',0,'주어진 사진을 주어진 라벨에 맞게 바운딩 박스를 치면 됩니다. \n','단, 박스를 칠 때 신호등은 켜져 있는 것만 바운딩 박스를 치면 됩니다.\n또한 간판, 신호등을 바운딩 박스를 칠 때, 꽉 차게 그리면 됩니다. ','도로이미지바운딩박스예시데이터.png','자율 주행 자동차 제작에 필요한 도로 간판, 신호등 이미지 바운딩 박스 작업입니다. \n주어진 도로 사진을 보고 라벨에 맞게 각각 바운딩 박스를 그리면 됩니다. ',20,5,0,1000),(27,5,'admin','어린이 보호구역 바운딩 박스','admin06251715','진행중','labelling','boundingBox','어린이 보호구역',0,'주어진 사진 속에서 어린이 보호구역 표지판을 바운딩 박스를 치면 됩니다. ','어린이 보호구역 표지판을 꽉 차게 바운딩 박스를 치면 됩니다.','예시데이터.png','자율 주행 자동차 제작을 위한 어린이 보호구역 표지판을 바운딩 박스를 치는 작업입니다.',10,0,0,500),(28,6,'admin','사과 분류','admin06251804','진행중','labelling','classification','사과',0,'주어지는 사과 사진을 보고 주어진 라벨에 맞게 분류하면 됩니다. ','어려운 문제이기 때문에 모르면 검색하셔서 작업을 진행해도 좋습니다. ','예시사과.png','사과 분류기 제작을 위한 종류별 사과 분류 작업입니다. \n주어지는 사과 사진을 보고 주어진 라벨에 맞게 분류하면 됩니다. ',6,4,0,300);
UNLOCK TABLES;

LOCK TABLES user_table WRITE;
DELETE FROM user_table;
INSERT INTO user_table VALUES (21,'admin','$2a$10$foMU0ysEMQhURf0rnRyo6ef74uBDE3QohDOofXw8S9Y6ZHXr3eAW2','관리자미스터킴','01012345678','admin@collectorinroom.kr','방구석 ',1,'2020-06-25 07:44:59',100000000,99996850,'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ',1100758491,0),(23,'high_user1','$2a$10$bodAXKT2THNV.nZc3pyfye2ONaArL9bS18SS2iDkvdN5Ulx42ZddC','김상일','01034567890','nahyun858@ajou.ac.kr','아주대학교',1,'2020-06-25 08:32:10',100,100,'4e50519587f747269112e34172833913',0,1),(24,'high_user2','$2a$10$Qml91/rcOoQ7Bb1LE4A.zehNzUQdCjilW7Y5HCjEwOtXIH8ymtelq','김상이','01044445555','woneyhoney@ajou.ac.kr','아주대학교',1,'2020-06-25 09:06:36',140,140,'5527123cf87c40a2b2270302fb0307d5',0,1),(25,'middle_user1','$2a$10$QjwF5zxZUAuBmYdJu2/G9ed8RLGMyx4m27JrtzMUAd1Y3NBZiaZc.','김중일','01099998888','tndus130@ajou.ac.kr','아주대학교',1,'2020-06-25 09:22:35',140,140,'4375d68b505f458d828f2b61c6bb0a3b',0,0.8333333333333334),(26,'middle_user2','$2a$10$6VQRZkHw4pGGnFgVqUD5s.ohRp/rLnQ.CUnbMGeHoEEyme/llimdS','김중이','01099998888','tndus130@naver.com','아주대학교',1,'2020-06-25 09:32:06',120,120,'405deef1d8124c40a37f7850204423b0',0,0.8333333333333334),(27,'low_user1','$2a$10$gxtpaUJ.RieyN4O6sEZloOU1ObdOo27c6yyOV0yk6hvK73YrYJB2y','김하일','01011111111','woneyhoney@ajou.ac.kr','방구석 ',2,'2020-07-01 19:26:46',200,200,'762f7afd37d248b6b90cbda619402aab',0,0.6666666666666666),(28,'requester','$2a$10$pRTO72eGZFlXM.edcZNVOO0UpfjH0j37vMgK0vzRRv33tiITYofhW','의뢰자','01027540421','wodnd999999@ajou.ac.kr','Ajou Univ.',30,'2020-06-25 00:00:00',100000,100000,'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ',1100758491,0),(29,'worker','$2a$10$pRTO72eGZFlXM.edcZNVOO0UpfjH0j37vMgK0vzRRv33tiITYofhW','작업자','01027540421','wodnd999999@ajou.ac.kr','Ajou Univ.',30,'2020-06-25 00:00:00',200000,200000,'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzU4NDkxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MDEzOTIyNTUsImp0aSI6ImExZGRiMDk4LTQ3ZTEtNDZiNy05OGZlLThhMzE2ZDQ2OTAyNSJ9.9lk6_-nmb84O_Gl2xx5tPLH7bQWfzmE2VRamOGHTnjQ',1100758491,0);
UNLOCK TABLES;