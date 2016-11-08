CREATE TABLE `appserver`.`portal` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `uri` VARCHAR(45) NOT NULL,
  `uid` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`, `uri`))
  UNIQUE (`uri`);

