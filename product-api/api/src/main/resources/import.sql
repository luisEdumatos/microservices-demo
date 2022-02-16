INSERT INTO CATEGORY (ID, DESCRIPTION) VALUES (1000, 'Comic Books');
INSERT INTO CATEGORY (ID, DESCRIPTION) VALUES (1001, 'Movies');
INSERT INTO CATEGORY (ID, DESCRIPTION) VALUES (1002, 'Books');

INSERT INTO SUPPLIER (ID, NAME) VALUES (1000, 'Panini Comics');
INSERT INTO SUPPLIER (ID, NAME) VALUES (1001, 'Amazon');

INSERT INTO PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) VALUES (1000, 'Crise nas Infinitas Terras', 1000, 1001, 10, CURRENT_TIMESTAMP);
INSERT INTO PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) VALUES (1001, 'Interestelar', 1001, 1001, 5, CURRENT_TIMESTAMP);
INSERT INTO PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) VALUES (1002, 'Harry Potter e a Pedra Filosofal', 1001, 1002, 3, CURRENT_TIMESTAMP);

