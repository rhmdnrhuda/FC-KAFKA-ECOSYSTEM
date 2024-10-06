CREATE TABLE user_transaction_aggregates
(
    customer_id  VARCHAR(50)    NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    currency     VARCHAR(10)    NOT NULL,
    PRIMARY KEY (customer_id)
);

CREATE TABLE transactions
(
    transaction_id   VARCHAR(255),
    product_id       VARCHAR(255),
    product_name     VARCHAR(255)   DEFAULT 'Unknown Product', -- Default value for product_name
    product_category VARCHAR(255)   DEFAULT 'General',         -- Default value for product_category
    product_price    DECIMAL(10, 2) DEFAULT 0.00,              -- Default value for product_price
    product_quantity INT            DEFAULT 1,                 -- Default value for product_quantity
    product_brand    VARCHAR(255)   DEFAULT 'Unknown Brand',   -- Default value for product_brand
    total_amount     DECIMAL(18, 2) DEFAULT 0.00,              -- Default value for total_amount
    currency         VARCHAR(3)     DEFAULT 'IDR',             -- Default value for currency
    customer_id      VARCHAR(255),
    transaction_date DATETIME       DEFAULT CURRENT_TIMESTAMP, -- Default value for transaction_date
    payment_method   VARCHAR(255)   DEFAULT 'Not Specified'    -- Default value for payment_method
);

CREATE TABLE transactions_back
(
    transaction_id   VARCHAR(255),
    product_id       VARCHAR(255),
    product_name     VARCHAR(255)   DEFAULT 'Unknown Product', -- Default value for product_name
    product_category VARCHAR(255)   DEFAULT 'General',         -- Default value for product_category
    product_price    DECIMAL(10, 2) DEFAULT 0.00,              -- Default value for product_price
    product_quantity INT            DEFAULT 1,                 -- Default value for product_quantity
    product_brand    VARCHAR(255)   DEFAULT 'Unknown Brand',   -- Default value for product_brand
    total_amount     DECIMAL(18, 2) DEFAULT 0.00,              -- Default value for total_amount
    currency         VARCHAR(3)     DEFAULT 'USD',             -- Default value for currency
    customer_id      VARCHAR(255),
    transaction_date DATETIME       DEFAULT CURRENT_TIMESTAMP, -- Default value for transaction_date
    payment_method   VARCHAR(255)   DEFAULT 'Not Specified'    -- Default value for payment_method
);