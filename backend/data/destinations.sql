-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mer. 21 jan. 2026 à 23:04
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `flask_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `destinations`
--

CREATE TABLE `destinations` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `continent` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `avgCostUSD` decimal(10,2) DEFAULT NULL,
  `bestSeason` varchar(50) DEFAULT NULL,
  `avgRating` decimal(3,2) DEFAULT NULL,
  `annualVisitors` decimal(10,2) DEFAULT NULL,
  `unescoSite` varchar(10) DEFAULT NULL,
  `photoURL` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `destinations`
--

INSERT INTO `destinations` (`id`, `name`, `country`, `continent`, `type`, `avgCostUSD`, `bestSeason`, `avgRating`, `annualVisitors`, `unescoSite`, `photoURL`) VALUES
(1, 'Serene Temple', 'Morocco', 'Africa', 'Beach', 174.84, 'Summer', 4.50, 7.45, 'No', 'https://images.pexels.com/photos/1007426/pexels-photo-1007426.jpeg?auto=compress&cs=tinysrgb&w=800'),
(2, 'Sacred Valley', 'Germany', 'Europe', 'Religious', 94.41, 'Summer', 4.60, 1.98, 'No', 'https://images.pexels.com/photos/208701/pexels-photo-208701.jpeg?auto=compress&cs=tinysrgb&w=800'),
(3, 'Serene Temple', 'South Africa', 'Africa', 'Adventure', 228.96, 'Summer', 4.70, 0.70, 'Yes', 'https://images.pexels.com/photos/259447/pexels-photo-259447.jpeg?auto=compress&cs=tinysrgb&w=800'),
(4, 'Sacred Plaza', 'Australia', 'Oceania', 'Nature', 120.96, 'Summer', 4.30, 2.24, 'No', 'https://images.pexels.com/photos/933054/pexels-photo-933054.jpeg?auto=compress&cs=tinysrgb&w=800'),
(5, 'Golden Ruins', 'Mexico', 'North America', 'Adventure', 162.10, 'Spring', 3.90, 4.60, 'No', 'https://images.pexels.com/photos/2166559/pexels-photo-2166559.jpeg?auto=compress&cs=tinysrgb&w=800'),
(6, 'Hidden Valley', 'Thailand', 'Asia', 'Historical', 190.82, 'Spring', 4.00, 1.83, 'Yes', 'https://images.pexels.com/photos/1007426/pexels-photo-1007426.jpeg?auto=compress&cs=tinysrgb&w=800'),
(7, 'Hidden Park', 'Spain', 'Europe', 'Adventure', 104.60, 'Summer', 4.10, 4.83, 'No', 'https://images.pexels.com/photos/3225531/pexels-photo-3225531.jpeg?auto=compress&cs=tinysrgb&w=800'),
(8, 'Grand Valley', 'Brazil', 'South America', 'Beach', 118.53, 'Autumn', 4.70, 5.39, 'No', 'https://images.pexels.com/photos/460621/pexels-photo-460621.jpeg?auto=compress&cs=tinysrgb&w=800'),
(9, 'Ancient Beach', 'New Zealand', 'Oceania', 'Historical', 122.78, 'Spring', 4.50, 6.27, 'No', 'https://images.pexels.com/photos/1450360/pexels-photo-1450360.jpeg?auto=compress&cs=tinysrgb&w=800'),
(10, 'Serene Valley', 'Kenya', 'Africa', 'City', 119.99, 'Spring', 4.80, 1.43, 'No', 'https://images.pexels.com/photos/1010657/pexels-photo-1010657.jpeg?auto=compress&cs=tinysrgb&w=800'),
(11, 'Hidden Ruins', 'China', 'Asia', 'Historical', 119.91, 'Autumn', 5.00, 1.66, 'No', 'https://images.pexels.com/photos/2592407/pexels-photo-2592407.jpeg?auto=compress&cs=tinysrgb&w=800'),
(12, 'Ancient Park', 'Japan', 'Asia', 'Historical', 123.32, 'Winter', 4.50, 9.14, 'No', 'https://images.pexels.com/photos/161401/fushimi-inari-taisha-shrine-kyoto-japan-temple-161401.jpeg?auto=compress&cs=tinysrgb&w=800'),
(13, 'Grand Canyon', 'Morocco', 'Africa', 'City', 191.13, 'Winter', 4.10, 3.46, 'Yes', 'https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800'),
(14, 'Golden Temple', 'Morocco', 'Africa', 'Religious', 288.92, 'Winter', 4.90, 2.26, 'No', 'https://images.pexels.com/photos/1141853/pexels-photo-1141853.jpeg?auto=compress&cs=tinysrgb&w=800'),
(15, 'Serene Park', 'Italy', 'Europe', 'Historical', 186.92, 'Autumn', 4.60, 1.34, 'Yes', 'https://images.pexels.com/photos/2064827/pexels-photo-2064827.jpeg?auto=compress&cs=tinysrgb&w=800'),
(16, 'Lush Ruins', 'Peru', 'South America', 'City', 99.55, 'Autumn', 4.00, 3.08, 'No', 'https://images.pexels.com/photos/2356045/pexels-photo-2356045.jpeg?auto=compress&cs=tinysrgb&w=800'),
(17, 'Sacred Canyon', 'Brazil', 'South America', 'City', 126.97, 'Winter', 4.80, 3.17, 'No', 'https://images.pexels.com/photos/351283/pexels-photo-351283.jpeg?auto=compress&cs=tinysrgb&w=800'),
(18, 'Mystic Forest', 'South Africa', 'Africa', 'Adventure', 93.47, 'Autumn', 4.50, 8.12, 'No', 'https://images.pexels.com/photos/1578750/pexels-photo-1578750.jpeg?auto=compress&cs=tinysrgb&w=800'),
(19, 'Mystic Ruins', 'Vietnam', 'Asia', 'Beach', 116.15, 'Summer', 4.70, 0.55, 'No', 'https://images.pexels.com/photos/1450353/pexels-photo-1450353.jpeg?auto=compress&cs=tinysrgb&w=800'),
(20, 'Lush Forest', 'Kenya', 'Africa', 'Adventure', 179.11, 'Summer', 4.80, 7.43, 'No', 'https://images.pexels.com/photos/631317/pexels-photo-631317.jpeg?auto=compress&cs=tinysrgb&w=800'),
(21, 'Ancient Forest', 'Brazil', 'South America', 'Nature', 108.04, 'Summer', 4.40, 8.70, 'No', 'https://images.pexels.com/photos/975771/pexels-photo-975771.jpeg?auto=compress&cs=tinysrgb&w=800'),
(22, 'Grand Ruins', 'Brazil', 'South America', 'Religious', 74.59, 'Spring', 4.80, 1.10, 'Yes', 'https://images.pexels.com/photos/3566187/pexels-photo-3566187.jpeg?auto=compress&cs=tinysrgb&w=800'),
(23, 'Mystic Forest', 'India', 'Asia', 'Beach', 94.68, 'Spring', 4.10, 7.43, 'No', 'https://images.pexels.com/photos/1450353/pexels-photo-1450353.jpeg?auto=compress&cs=tinysrgb&w=800'),
(24, 'Sacred Park', 'Greece', 'Europe', 'Religious', 191.74, 'Spring', 4.60, 4.99, 'No', 'https://images.pexels.com/photos/164336/pexels-photo-164336.jpeg?auto=compress&cs=tinysrgb&w=800'),
(25, 'Sacred Forest', 'Canada', 'North America', 'City', 168.08, 'Spring', 4.30, 7.73, 'No', 'https://images.pexels.com/photos/1519088/pexels-photo-1519088.jpeg?auto=compress&cs=tinysrgb&w=800'),
(26, 'Ancient Falls', 'France', 'Europe', 'Nature', 224.77, 'Spring', 4.60, 5.19, 'No', 'https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=800'),
(27, 'Ancient Valley', 'Argentina', 'South America', 'City', 30.00, 'Summer', 4.70, 0.74, 'Yes', 'https://images.pexels.com/photos/951531/pexels-photo-951531.jpeg?auto=compress&cs=tinysrgb&w=800'),
(28, 'Lush Forest', 'Vietnam', 'Asia', 'Beach', 79.06, 'Winter', 4.80, 5.33, 'Yes', 'https://images.pexels.com/photos/1268871/pexels-photo-1268871.jpeg?auto=compress&cs=tinysrgb&w=800'),
(29, 'Sacred Temple', 'USA', 'North America', 'Historical', 139.02, 'Autumn', 4.60, 4.40, 'No', 'https://images.pexels.com/photos/221148/pexels-photo-221148.jpeg?auto=compress&cs=tinysrgb&w=800'),
(30, 'Sacred Pagoda', 'Argentina', 'South America', 'Adventure', 110.54, 'Spring', 4.70, 1.23, 'No', 'https://images.pexels.com/photos/2263436/pexels-photo-2263436.jpeg?auto=compress&cs=tinysrgb&w=800'),
(31, 'Golden Park', 'Greece', 'Europe', 'City', 109.58, 'Winter', 4.30, 9.33, 'Yes', 'https://images.pexels.com/photos/1285625/pexels-photo-1285625.jpeg?auto=compress&cs=tinysrgb&w=800'),
(32, 'Golden Temple', 'New Zealand', 'Oceania', 'City', 175.07, 'Winter', 4.80, 8.78, 'Yes', 'https://images.pexels.com/photos/1450360/pexels-photo-1450360.jpeg?auto=compress&cs=tinysrgb&w=800'),
(33, 'Serene Canyon', 'Spain', 'Europe', 'Beach', 123.51, 'Spring', 4.70, 8.98, 'Yes', 'https://images.pexels.com/photos/1660995/pexels-photo-1660995.jpeg?auto=compress&cs=tinysrgb&w=800'),
(34, 'Lush Temple', 'Kenya', 'Africa', 'Adventure', 218.59, 'Winter', 4.60, 9.01, 'No', 'https://images.pexels.com/photos/1183099/pexels-photo-1183099.jpeg?auto=compress&cs=tinysrgb&w=800'),
(35, 'Grand Falls', 'Vietnam', 'Asia', 'Adventure', 114.90, 'Winter', 4.40, 2.67, 'Yes', 'https://images.pexels.com/photos/585759/pexels-photo-585759.jpeg?auto=compress&cs=tinysrgb&w=800'),
(36, 'Golden Valley', 'Thailand', 'Asia', 'Adventure', 213.69, 'Autumn', 4.40, 8.68, 'No', 'https://images.pexels.com/photos/1007657/pexels-photo-1007657.jpeg?auto=compress&cs=tinysrgb&w=800'),
(37, 'Ancient Temple', 'Italy', 'Europe', 'Adventure', 150.26, 'Spring', 4.40, 4.47, 'No', 'https://images.pexels.com/photos/2064827/pexels-photo-2064827.jpeg?auto=compress&cs=tinysrgb&w=800'),
(38, 'Mystic Canyon', 'Canada', 'North America', 'City', 130.22, 'Autumn', 4.40, 3.71, 'Yes', 'https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800'),
(39, 'Sacred Valley', 'India', 'Asia', 'City', 141.94, 'Summer', 4.60, 5.43, 'No', 'https://images.pexels.com/photos/1098460/pexels-photo-1098460.jpeg?auto=compress&cs=tinysrgb&w=800'),
(40, 'Serene Ruins', 'Italy', 'Europe', 'Religious', 102.85, 'Summer', 4.90, 9.73, 'No', 'https://images.pexels.com/photos/2404949/pexels-photo-2404949.jpeg?auto=compress&cs=tinysrgb&w=800'),
(41, 'Grand Beach', 'Thailand', 'Asia', 'Adventure', 54.06, 'Autumn', 4.50, 3.21, 'Yes', 'https://images.pexels.com/photos/1007426/pexels-photo-1007426.jpeg?auto=compress&cs=tinysrgb&w=800'),
(42, 'Crystal Pagoda', 'USA', 'North America', 'Adventure', 155.12, 'Summer', 4.40, 5.28, 'No', 'https://images.pexels.com/photos/1450360/pexels-photo-1450360.jpeg?auto=compress&cs=tinysrgb&w=800'),
(43, 'Crystal Temple', 'Argentina', 'South America', 'Adventure', 140.38, 'Autumn', 4.60, 1.88, 'Yes', 'https://images.pexels.com/photos/2263436/pexels-photo-2263436.jpeg?auto=compress&cs=tinysrgb&w=800'),
(44, 'Golden Beach', 'Italy', 'Europe', 'Nature', 166.99, 'Summer', 4.50, 2.80, 'No', 'https://images.pexels.com/photos/1450363/pexels-photo-1450363.jpeg?auto=compress&cs=tinysrgb&w=800'),
(45, 'Sacred Valley', 'South Africa', 'Africa', 'Adventure', 207.14, 'Winter', 4.70, 2.76, 'Yes', 'https://images.pexels.com/photos/631317/pexels-photo-631317.jpeg?auto=compress&cs=tinysrgb&w=800'),
(46, 'Serene Plaza', 'USA', 'North America', 'Beach', 109.91, 'Summer', 4.90, 6.51, 'No', 'https://images.pexels.com/photos/1450353/pexels-photo-1450353.jpeg?auto=compress&cs=tinysrgb&w=800'),
(47, 'Golden Park', 'Greece', 'Europe', 'Nature', 179.34, 'Winter', 5.00, 1.36, 'No', 'https://images.pexels.com/photos/1285625/pexels-photo-1285625.jpeg?auto=compress&cs=tinysrgb&w=800'),
(48, 'Serene Falls', 'Spain', 'Europe', 'Adventure', 125.33, 'Spring', 4.80, 2.27, 'No', 'https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=800'),
(49, 'Crystal Pagoda', 'Mexico', 'North America', 'Historical', 154.98, 'Autumn', 4.30, 6.94, 'Yes', 'https://images.pexels.com/photos/2166559/pexels-photo-2166559.jpeg?auto=compress&cs=tinysrgb&w=800'),
(50, 'Crystal Pagoda', 'Mexico', 'North America', 'Historical', 154.98, 'Autumn', 4.30, 6.94, 'Yes', 'https://images.pexels.com/photos/mexico-pagoda-historical.jpg'),
(51, 'Ancient Valley', 'Peru', 'South America', 'Beach', 150.46, 'Summer', 4.40, 2.65, 'No', 'https://images.pexels.com/photos/peru-valley-beach.jpg'),
(52, 'Hidden Valley', 'New Zealand', 'Oceania', 'City', 96.88, 'Spring', 4.60, 7.06, 'Yes', 'https://images.pexels.com/photos/new-zealand-valley-city.jpg'),
(53, 'Sacred Canyon', 'Mexico', 'North America', 'Historical', 181.03, 'Spring', 4.50, 1.81, 'Yes', 'https://images.pexels.com/photos/mexico-canyon-historical.jpg'),
(54, 'Sacred Canyon', 'Egypt', 'Africa', 'Religious', 110.84, 'Summer', 4.40, 9.28, 'No', 'https://images.pexels.com/photos/egypt-canyon-religious.jpg'),
(55, 'Ancient Forest', 'Kenya', 'Africa', 'Beach', 132.16, 'Winter', 4.70, 6.77, 'Yes', 'https://images.pexels.com/photos/kenya-forest-beach.jpg'),
(56, 'Hidden Temple', 'Germany', 'Europe', 'Historical', 161.37, 'Spring', 4.90, 5.53, 'No', 'https://images.pexels.com/photos/germany-temple-historical.jpg'),
(57, 'Crystal Beach', 'Kenya', 'Africa', 'Religious', 133.78, 'Spring', 4.40, 9.02, 'No', 'https://images.pexels.com/photos/kenya-beach-religious.jpg'),
(58, 'Golden Beach', 'Spain', 'Europe', 'Religious', 162.99, 'Autumn', 4.70, 3.72, 'No', 'https://images.pexels.com/photos/spain-beach-religious.jpg'),
(59, 'Lush Forest', 'Peru', 'South America', 'City', 214.97, 'Spring', 4.20, 9.02, 'Yes', 'https://images.pexels.com/photos/peru-forest-city.jpg'),
(60, 'Mystic Forest', 'Thailand', 'Asia', 'Nature', 162.52, 'Summer', 4.60, 6.60, 'Yes', 'https://images.pexels.com/photos/thailand-forest-nature.jpg'),
(61, 'Golden Canyon', 'Italy', 'Europe', 'Historical', 161.31, 'Winter', 4.80, 0.59, 'No', 'https://images.pexels.com/photos/italy-canyon-historical.jpg'),
(62, 'Serene Valley', 'Japan', 'Asia', 'Beach', 164.65, 'Spring', 4.30, 0.55, 'Yes', 'https://images.pexels.com/photos/japan-valley-beach.jpg'),
(63, 'Mystic Temple', 'Japan', 'Asia', 'Nature', 158.74, 'Autumn', 4.10, 7.07, 'No', 'https://images.pexels.com/photos/japan-temple-nature.jpg'),
(64, 'Hidden Plaza', 'USA', 'North America', 'City', 90.43, 'Autumn', 4.70, 7.27, 'Yes', 'https://images.pexels.com/photos/usa-plaza-city.jpg'),
(65, 'Ancient Plaza', 'Kenya', 'Africa', 'Religious', 112.44, 'Winter', 4.20, 7.59, 'Yes', 'https://images.pexels.com/photos/kenya-plaza-religious.jpg'),
(66, 'Lush Falls', 'Italy', 'Europe', 'Adventure', 198.17, 'Winter', 4.60, 6.75, 'Yes', 'https://images.pexels.com/photos/italy-waterfall-adventure.jpg'),
(67, 'Serene Plaza', 'Argentina', 'South America', 'City', 106.62, 'Winter', 4.50, 3.99, 'Yes', 'https://images.pexels.com/photos/argentina-plaza-city.jpg'),
(68, 'Crystal Falls', 'Peru', 'South America', 'Historical', 105.52, 'Summer', 4.30, 9.74, 'No', 'https://images.pexels.com/photos/peru-waterfall-historical.jpg'),
(69, 'Sacred Falls', 'Morocco', 'Africa', 'Nature', 193.93, 'Autumn', 4.40, 6.50, 'No', 'https://images.pexels.com/photos/morocco-waterfall-nature.jpg'),
(70, 'Crystal Beach', 'France', 'Europe', 'Beach', 150.65, 'Spring', 4.90, 5.98, 'Yes', 'https://images.pexels.com/photos/france-beach-crystal.jpg'),
(71, 'Hidden Plaza', 'Greece', 'Europe', 'Nature', 79.68, 'Autumn', 4.50, 7.36, 'No', 'https://images.pexels.com/photos/greece-plaza-nature.jpg'),
(72, 'Serene Canyon', 'Egypt', 'Africa', 'Nature', 96.46, 'Summer', 4.60, 9.43, 'No', 'https://images.pexels.com/photos/egypt-canyon-nature.jpg'),
(73, 'Sacred Temple', 'Italy', 'Europe', 'Religious', 143.79, 'Autumn', 4.70, 9.68, 'Yes', 'https://images.pexels.com/photos/italy-temple-religious.jpg'),
(74, 'Hidden Park', 'Japan', 'Asia', 'Adventure', 107.66, 'Summer', 4.00, 8.59, 'No', 'https://images.pexels.com/photos/japan-park-adventure.jpg'),
(75, 'Crystal Forest', 'Peru', 'South America', 'Religious', 103.70, 'Summer', 4.30, 5.79, 'No', 'https://images.pexels.com/photos/peru-forest-religious.jpg'),
(76, 'Crystal Forest', 'Argentina', 'South America', 'Religious', 158.66, 'Autumn', 4.60, 5.92, 'No', 'https://images.pexels.com/photos/argentina-forest-religious.jpg'),
(77, 'Grand Ruins', 'Germany', 'Europe', 'City', 161.55, 'Autumn', 4.30, 9.91, 'No', 'https://images.pexels.com/photos/germany-ruins-city.jpg'),
(78, 'Golden Canyon', 'Egypt', 'Africa', 'Nature', 152.91, 'Spring', 4.20, 8.84, 'No', 'https://images.pexels.com/photos/egypt-canyon-nature.jpg'),
(79, 'Hidden Pagoda', 'Argentina', 'South America', 'City', 193.62, 'Autumn', 4.80, 7.17, 'No', 'https://images.pexels.com/photos/argentina-pagoda-city.jpg'),
(80, 'Mystic Valley', 'India', 'Asia', 'Adventure', 81.12, 'Winter', 4.20, 8.19, 'No', 'https://images.pexels.com/photos/india-valley-adventure.jpg'),
(81, 'Serene Canyon', 'Argentina', 'South America', 'Nature', 165.22, 'Spring', 4.60, 9.18, 'Yes', 'https://images.pexels.com/photos/argentina-canyon-nature.jpg'),
(82, 'Serene Pagoda', 'Mexico', 'North America', 'City', 175.75, 'Winter', 5.00, 8.08, 'Yes', 'https://images.pexels.com/photos/mexico-pagoda-city.jpg'),
(83, 'Serene Plaza', 'Egypt', 'Africa', 'Beach', 216.54, 'Autumn', 4.80, 8.06, 'No', 'https://images.pexels.com/photos/egypt-plaza-beach.jpg'),
(84, 'Golden Valley', 'Vietnam', 'Asia', 'Adventure', 134.24, 'Winter', 4.70, 4.07, 'Yes', 'https://images.pexels.com/photos/vietnam-valley-adventure.jpg'),
(85, 'Sacred Pagoda', 'Thailand', 'Asia', 'City', 158.25, 'Winter', 4.20, 0.84, 'No', 'https://images.pexels.com/photos/thailand-pagoda-city.jpg'),
(86, 'Ancient Pagoda', 'Italy', 'Europe', 'Nature', 265.73, 'Winter', 3.90, 3.22, 'No', 'https://images.pexels.com/photos/italy-pagoda-nature.jpg'),
(87, 'Lush Park', 'Germany', 'Europe', 'Adventure', 129.27, 'Spring', 4.50, 0.85, 'No', 'https://images.pexels.com/photos/germany-park-adventure.jpg'),
(88, 'Lush Pagoda', 'Australia', 'Oceania', 'Adventure', 126.40, 'Autumn', 4.80, 1.71, 'No', 'https://images.pexels.com/photos/australia-pagoda-adventure.jpg'),
(89, 'Sacred Pagoda', 'Brazil', 'South America', 'City', 228.02, 'Spring', 4.50, 2.55, 'No', 'https://images.pexels.com/photos/brazil-pagoda-city.jpg'),
(90, 'Crystal Temple', 'Morocco', 'Africa', 'City', 113.48, 'Spring', 4.60, 0.99, 'No', 'https://images.pexels.com/photos/morocco-temple-city.jpg'),
(91, 'Ancient Ruins', 'Brazil', 'South America', 'Historical', 269.45, 'Spring', 5.00, 6.56, 'No', 'https://images.pexels.com/photos/brazil-ruins-historical.jpg'),
(92, 'Serene Valley', 'Germany', 'Europe', 'City', 48.74, 'Autumn', 4.60, 8.05, 'No', 'https://images.pexels.com/photos/germany-valley-city.jpg'),
(93, 'Grand Beach', 'Thailand', 'Asia', 'Adventure', 127.78, 'Spring', 4.00, 1.25, 'No', 'https://images.pexels.com/photos/thailand-beach-adventure.jpg'),
(94, 'Golden Park', 'Canada', 'North America', 'Adventure', 175.25, 'Autumn', 4.80, 4.39, 'No', 'https://images.pexels.com/photos/canada-park-adventure.jpg'),
(95, 'Crystal Plaza', 'China', 'Asia', 'Adventure', 133.29, 'Summer', 4.40, 2.88, 'No', 'https://images.pexels.com/photos/china-plaza-adventure.jpg'),
(96, 'Crystal Plaza', 'Thailand', 'Asia', 'Nature', 238.27, 'Winter', 4.60, 6.77, 'No', 'https://images.pexels.com/photos/thailand-plaza-nature.jpg'),
(97, 'Grand Ruins', 'Vietnam', 'Asia', 'Beach', 168.86, 'Autumn', 5.00, 6.31, 'No', 'https://images.pexels.com/photos/vietnam-ruins-beach.jpg'),
(98, 'Golden Valley', 'India', 'Asia', 'City', 74.03, 'Summer', 4.40, 3.88, 'No', 'https://images.pexels.com/photos/india-valley-city.jpg'),
(99, 'Mystic Beach', 'Morocco', 'Africa', 'Adventure', 138.13, 'Winter', 5.00, 1.43, 'No', 'https://images.pexels.com/photos/morocco-beach-adventure.jpg'),
(100, 'Hidden Falls', 'Brazil', 'South America', 'Adventure', 30.00, 'Spring', 4.20, 2.15, 'Yes', 'https://images.pexels.com/photos/brazil-waterfall-adventure.jpg'),
(101, 'Ancient Pagoda', 'China', 'Asia', 'Historical', 50.25, 'Autumn', 4.10, 6.35, 'Yes', 'https://images.pexels.com/photos/china-pagoda-ancient.jpg'),
(102, 'Hidden Temple', 'France', 'Europe', 'Religious', 128.00, 'Summer', 4.50, 4.06, 'No', 'https://images.pexels.com/photos/france-temple-religious.jpg'),
(103, 'Serene Temple', 'Morocco', 'Africa', 'Beach', 152.93, 'Summer', 5.00, 8.64, 'No', 'https://images.pexels.com/photos/morocco-temple-beach.jpg'),
(104, 'Lush Valley', 'France', 'Europe', 'Nature', 100.92, 'Winter', 4.60, 1.17, 'Yes', 'https://images.pexels.com/photos/france-valley-nature.jpg'),
(105, 'Hidden Ruins', 'South Africa', 'Africa', 'City', 139.88, 'Autumn', 4.50, 6.06, 'No', 'https://images.pexels.com/photos/south-africa-ruins-city.jpg'),
(106, 'Mystic Canyon', 'Egypt', 'Africa', 'Nature', 155.68, 'Summer', 4.70, 4.19, 'No', 'https://images.pexels.com/photos/egypt-canyon-nature.jpg'),
(107, 'Grand Plaza', 'Greece', 'Europe', 'Nature', 119.24, 'Summer', 5.00, 5.68, 'No', 'https://images.pexels.com/photos/greece-plaza-nature.jpg'),
(108, 'Crystal Park', 'Canada', 'North America', 'Beach', 142.41, 'Winter', 4.70, 9.63, 'Yes', 'https://images.pexels.com/photos/canada-park-beach.jpg'),
(109, 'Grand Falls', 'Greece', 'Europe', 'Nature', 125.68, 'Summer', 4.60, 3.53, 'No', 'https://images.pexels.com/photos/greece-waterfall-nature.jpg'),
(110, 'Grand Pagoda', 'Spain', 'Europe', 'Nature', 120.53, 'Spring', 4.80, 1.62, 'Yes', 'https://images.pexels.com/photos/spain-pagoda-nature.jpg'),
(111, 'Lush Ruins', 'Greece', 'Europe', 'Adventure', 197.60, 'Spring', 4.90, 8.84, 'No', 'https://images.pexels.com/photos/greece-ruins-adventure.jpg'),
(112, 'Sacred Pagoda', 'Thailand', 'Asia', 'City', 190.64, 'Spring', 4.70, 3.18, 'No', 'https://images.pexels.com/photos/thailand-pagoda-city.jpg'),
(113, 'Serene Valley', 'India', 'Asia', 'Adventure', 177.59, 'Spring', 4.30, 8.16, 'No', 'https://images.pexels.com/photos/india-valley-adventure.jpg'),
(114, 'Grand Falls', 'Brazil', 'South America', 'Adventure', 148.95, 'Winter', 4.50, 4.03, 'No', 'https://images.pexels.com/photos/brazil-waterfall-adventure.jpg'),
(115, 'Serene Falls', 'France', 'Europe', 'Adventure', 116.54, 'Spring', 4.80, 9.34, 'Yes', 'https://images.pexels.com/photos/france-waterfall-adventure.jpg'),
(116, 'Crystal Forest', 'Brazil', 'South America', 'Historical', 139.12, 'Spring', 4.80, 7.63, 'No', 'https://images.pexels.com/photos/brazil-forest-historical.jpg'),
(117, 'Grand Pagoda', 'France', 'Europe', 'Nature', 129.60, 'Winter', 4.60, 9.07, 'Yes', 'https://images.pexels.com/photos/france-pagoda-nature.jpg'),
(118, 'Lush Pagoda', 'New Zealand', 'Oceania', 'City', 215.27, 'Autumn', 4.50, 3.54, 'No', 'https://images.pexels.com/photos/new-zealand-pagoda-city.jpg'),
(119, 'Serene Canyon', 'France', 'Europe', 'Beach', 138.04, 'Spring', 4.80, 0.60, 'Yes', 'https://images.pexels.com/photos/france-canyon-beach.jpg'),
(120, 'Sacred Falls', 'Kenya', 'Africa', 'Nature', 166.21, 'Spring', 4.50, 9.53, 'No', 'https://images.pexels.com/photos/kenya-waterfall-nature.jpg'),
(121, 'Lush Canyon', 'Thailand', 'Asia', 'Religious', 245.62, 'Spring', 4.80, 4.76, 'No', 'https://images.pexels.com/photos/thailand-canyon-religious.jpg'),
(122, 'Serene Falls', 'Argentina', 'South America', 'Historical', 99.70, 'Winter', 4.10, 6.89, 'Yes', 'https://images.pexels.com/photos/argentina-waterfall-historical.jpg'),
(123, 'Lush Canyon', 'Greece', 'Europe', 'Religious', 188.53, 'Winter', 4.70, 8.00, 'Yes', 'https://images.pexels.com/photos/greece-canyon-religious.jpg'),
(124, 'Serene Canyon', 'South Africa', 'Africa', 'Nature', 149.39, 'Spring', 4.20, 1.05, 'No', 'https://images.pexels.com/photos/south-africa-canyon-nature.jpg'),
(125, 'Sacred Ruins', 'Mexico', 'North America', 'Historical', 45.50, 'Winter', 5.00, 8.93, 'No', 'https://images.pexels.com/photos/mexico-ruins-historical.jpg'),
(126, 'Mystic Pagoda', 'Brazil', 'South America', 'Beach', 108.73, 'Summer', 4.40, 1.86, 'No', 'https://images.pexels.com/photos/brazil-pagoda-beach.jpg'),
(127, 'Lush Falls', 'Greece', 'Europe', 'Beach', 180.69, 'Spring', 4.90, 1.46, 'No', 'https://images.pexels.com/photos/greece-waterfall-beach.jpg'),
(128, 'Lush Valley', 'USA', 'North America', 'Beach', 162.25, 'Spring', 4.30, 1.19, 'No', 'https://images.pexels.com/photos/usa-valley-beach.jpg'),
(129, 'Golden Ruins', 'Spain', 'Europe', 'Adventure', 177.96, 'Autumn', 4.80, 1.27, 'Yes', 'https://images.pexels.com/photos/spain-ruins-adventure.jpg'),
(130, 'Serene Valley', 'Germany', 'Europe', 'Nature', 77.60, 'Spring', 4.10, 8.22, 'Yes', 'https://images.pexels.com/photos/germany-valley-nature.jpg'),
(131, 'Grand Pagoda', 'New Zealand', 'Oceania', 'Adventure', 116.79, 'Winter', 4.90, 1.29, 'No', 'https://images.pexels.com/photos/new-zealand-pagoda-adventure.jpg'),
(132, 'Golden Park', 'India', 'Asia', 'Historical', 165.55, 'Autumn', 4.90, 4.53, 'No', 'https://images.pexels.com/photos/india-park-historical.jpg'),
(133, 'Ancient Ruins', 'Greece', 'Europe', 'City', 134.88, 'Winter', 4.50, 4.95, 'Yes', 'https://images.pexels.com/photos/greece-ruins-city.jpg'),
(134, 'Grand Pagoda', 'Mexico', 'North America', 'City', 166.14, 'Summer', 4.30, 7.59, 'Yes', 'https://images.pexels.com/photos/mexico-pagoda-city.jpg'),
(135, 'Crystal Canyon', 'Egypt', 'Africa', 'Adventure', 174.63, 'Spring', 4.50, 4.06, 'Yes', 'https://images.pexels.com/photos/egypt-canyon-adventure.jpg'),
(136, 'Serene Plaza', 'India', 'Asia', 'Beach', 184.51, 'Winter', 4.40, 2.62, 'No', 'https://images.pexels.com/photos/india-plaza-beach.jpg'),
(137, 'Crystal Ruins', 'Greece', 'Europe', 'Nature', 153.48, 'Winter', 4.80, 9.93, 'No', 'https://images.pexels.com/photos/greece-ruins-nature.jpg'),
(138, 'Crystal Pagoda', 'Spain', 'Europe', 'Beach', 154.88, 'Winter', 4.30, 9.71, 'No', 'https://images.pexels.com/photos/spain-pagoda-beach.jpg'),
(139, 'Crystal Forest', 'Greece', 'Europe', 'Adventure', 263.79, 'Spring', 4.60, 7.11, 'No', 'https://images.pexels.com/photos/greece-forest-adventure.jpg'),
(140, 'Golden Plaza', 'New Zealand', 'Oceania', 'Adventure', 257.66, 'Autumn', 4.30, 6.05, 'Yes', 'https://images.pexels.com/photos/new-zealand-plaza-adventure.jpg'),
(141, 'Serene Park', 'Peru', 'South America', 'Nature', 144.45, 'Spring', 4.80, 6.39, 'Yes', 'https://images.pexels.com/photos/peru-park-nature.jpg'),
(142, 'Mystic Pagoda', 'South Africa', 'Africa', 'Beach', 108.01, 'Spring', 4.30, 4.91, 'No', 'https://images.pexels.com/photos/south-africa-pagoda-beach.jpg'),
(143, 'Mystic Park', 'Australia', 'Oceania', 'Religious', 201.63, 'Autumn', 4.00, 1.24, 'No', 'https://images.pexels.com/photos/australia-park-religious.jpg'),
(144, 'Lush Falls', 'Thailand', 'Asia', 'Religious', 167.09, 'Autumn', 5.00, 3.44, 'No', 'https://images.pexels.com/photos/thailand-waterfall-religious.jpg'),
(145, 'Mystic Falls', 'Thailand', 'Asia', 'Historical', 178.57, 'Spring', 4.80, 2.04, 'No', 'https://images.pexels.com/photos/thailand-waterfall-historical.jpg'),
(146, 'Grand Forest', 'Thailand', 'Asia', 'Religious', 83.99, 'Autumn', 5.00, 9.36, 'Yes', 'https://images.pexels.com/photos/thailand-forest-religious.jpg'),
(147, 'Hidden Valley', 'Italy', 'Europe', 'Adventure', 146.32, 'Summer', 4.40, 8.20, 'No', 'https://images.pexels.com/photos/italy-valley-adventure.jpg'),
(148, 'Sacred Pagoda', 'China', 'Asia', 'Nature', 64.34, 'Summer', 4.90, 9.71, 'No', 'https://images.pexels.com/photos/china-pagoda-nature.jpg'),
(149, 'Lush Pagoda', 'Thailand', 'Asia', 'Beach', 163.78, 'Winter', 4.60, 4.95, 'No', 'https://images.pexels.com/photos/thailand-pagoda-beach.jpg'),
(150, 'Crystal Pagoda', 'Italy', 'Europe', 'City', 70.28, 'Spring', 4.30, 1.04, 'No', 'https://images.pexels.com/photos/italy-pagoda-city.jpg'),
(151, 'Serene Ruins', 'Vietnam', 'Asia', 'Beach', 162.97, 'Summer', 4.60, 10.00, 'No', 'https://images.pexels.com/photos/vietnam-ruins-beach.jpg'),
(152, 'Ancient Temple', 'Argentina', 'South America', 'Nature', 150.26, 'Summer', 4.50, 7.81, 'No', 'https://images.pexels.com/photos/argentina-temple-nature.jpg'),
(153, 'Mystic Pagoda', 'Greece', 'Europe', 'Adventure', 134.35, 'Summer', 4.00, 1.73, 'No', 'https://images.pexels.com/photos/greece-pagoda-adventure.jpg'),
(154, 'Lush Pagoda', 'Argentina', 'South America', 'Nature', 156.01, 'Spring', 4.70, 2.67, 'No', 'https://images.pexels.com/photos/argentina-pagoda-nature.jpg'),
(155, 'Sacred Ruins', 'Mexico', 'North America', 'Adventure', 203.02, 'Winter', 5.00, 3.90, 'No', 'https://images.pexels.com/photos/mexico-ruins-adventure.jpg'),
(156, 'Sacred Canyon', 'Spain', 'Europe', 'Historical', 166.62, 'Autumn', 4.30, 5.44, 'No', 'https://images.pexels.com/photos/spain-canyon-historical.jpg'),
(157, 'Lush Ruins', 'France', 'Europe', 'Historical', 155.74, 'Spring', 5.00, 8.60, 'No', 'https://images.pexels.com/photos/france-ruins-historical.jpg'),
(158, 'Sacred Forest', 'India', 'Asia', 'Nature', 253.04, 'Autumn', 5.00, 8.83, 'No', 'https://images.pexels.com/photos/india-forest-nature.jpg'),
(159, 'Serene Park', 'China', 'Asia', 'Adventure', 98.98, 'Spring', 4.40, 0.77, 'No', 'https://images.pexels.com/photos/china-park-adventure.jpg'),
(160, 'Lush Plaza', 'China', 'Asia', 'City', 182.27, 'Autumn', 4.90, 7.19, 'Yes', 'https://images.pexels.com/photos/china-plaza-city.jpg'),
(161, 'Golden Ruins', 'China', 'Asia', 'Religious', 128.32, 'Winter', 4.40, 0.64, 'No', 'https://images.pexels.com/photos/china-ruins-religious.jpg'),
(162, 'Lush Valley', 'Australia', 'Oceania', 'Nature', 202.92, 'Spring', 4.00, 4.23, 'No', 'https://images.pexels.com/photos/australia-valley-nature.jpg'),
(163, 'Grand Ruins', 'Australia', 'Oceania', 'Adventure', 194.30, 'Summer', 4.50, 3.81, 'No', 'https://images.pexels.com/photos/australia-ruins-adventure.jpg'),
(164, 'Grand Valley', 'Spain', 'Europe', 'Adventure', 225.12, 'Spring', 4.50, 4.27, 'No', 'https://images.pexels.com/photos/spain-valley-adventure.jpg'),
(165, 'Lush Forest', 'Peru', 'South America', 'Religious', 199.08, 'Autumn', 4.60, 9.52, 'No', 'https://images.pexels.com/photos/peru-forest-religious.jpg'),
(166, 'Crystal Temple', 'Italy', 'Europe', 'Nature', 64.83, 'Summer', 4.50, 4.86, 'Yes', 'https://images.pexels.com/photos/italy-temple-nature.jpg'),
(167, 'Ancient Pagoda', 'Spain', 'Europe', 'Adventure', 149.69, 'Summer', 4.60, 3.62, 'No', 'https://images.pexels.com/photos/spain-pagoda-adventure.jpg'),
(168, 'Golden Falls', 'USA', 'North America', 'Religious', 84.78, 'Spring', 4.70, 1.22, 'No', 'https://images.pexels.com/photos/usa-waterfall-religious.jpg'),
(169, 'Mystic Temple', 'Italy', 'Europe', 'Historical', 94.94, 'Spring', 4.60, 3.78, 'No', 'https://images.pexels.com/photos/italy-temple-historical.jpg'),
(170, 'Ancient Park', 'Australia', 'Oceania', 'Adventure', 146.87, 'Winter', 4.80, 6.84, 'Yes', 'https://images.pexels.com/photos/australia-park-adventure.jpg'),
(171, 'Mystic Temple', 'China', 'Asia', 'Nature', 127.67, 'Winter', 4.40, 0.89, 'No', 'https://images.pexels.com/photos/china-temple-nature.jpg'),
(172, 'Golden Pagoda', 'India', 'Asia', 'Religious', 123.49, 'Spring', 4.30, 2.18, 'No', 'https://images.pexels.com/photos/india-pagoda-religious.jpg'),
(173, 'Hidden Park', 'Argentina', 'South America', 'Religious', 78.30, 'Winter', 4.40, 3.96, 'Yes', 'https://images.pexels.com/photos/argentina-park-religious.jpg'),
(174, 'Lush Canyon', 'Argentina', 'South America', 'Religious', 248.24, 'Autumn', 4.50, 0.87, 'No', 'https://images.pexels.com/photos/argentina-canyon-religious.jpg'),
(175, 'Grand Park', 'Canada', 'North America', 'Adventure', 175.73, 'Autumn', 4.90, 1.28, 'Yes', 'https://images.pexels.com/photos/canada-park-adventure.jpg'),
(176, 'Crystal Beach', 'Japan', 'Asia', 'Nature', 144.38, 'Spring', 4.40, 8.16, 'No', 'https://images.pexels.com/photos/japan-beach-nature.jpg'),
(177, 'Golden Park', 'Kenya', 'Africa', 'Adventure', 113.18, 'Spring', 4.70, 2.49, 'Yes', 'https://images.pexels.com/photos/kenya-park-adventure.jpg'),
(178, 'Hidden Temple', 'Vietnam', 'Asia', 'Historical', 136.25, 'Winter', 3.80, 6.37, 'No', 'https://images.pexels.com/photos/vietnam-temple-historical.jpg'),
(179, 'Mystic Valley', 'Morocco', 'Africa', 'City', 118.67, 'Spring', 3.80, 7.60, 'No', 'https://images.pexels.com/photos/morocco-valley-city.jpg'),
(180, 'Lush Beach', 'Morocco', 'Africa', 'Religious', 178.83, 'Summer', 4.60, 5.36, 'No', 'https://images.pexels.com/photos/morocco-beach-religious.jpg'),
(181, 'Crystal Falls', 'South Africa', 'Africa', 'Historical', 101.39, 'Winter', 4.50, 4.75, 'Yes', 'https://images.pexels.com/photos/south-africa-waterfall-historical.jpg'),
(182, 'Sacred Falls', 'Thailand', 'Asia', 'City', 69.68, 'Winter', 4.60, 3.06, 'Yes', 'https://images.pexels.com/photos/thailand-waterfall-city.jpg'),
(183, 'Crystal Pagoda', 'Australia', 'Oceania', 'Nature', 140.65, 'Spring', 4.50, 3.56, 'No', 'https://images.pexels.com/photos/australia-pagoda-nature.jpg'),
(184, 'Lush Valley', 'Argentina', 'South America', 'Religious', 117.67, 'Spring', 4.20, 1.64, 'No', 'https://images.pexels.com/photos/argentina-valley-religious.jpg'),
(185, 'Sacred Falls', 'Germany', 'Europe', 'City', 160.91, 'Autumn', 4.80, 6.95, 'No', 'https://images.pexels.com/photos/germany-waterfall-city.jpg'),
(186, 'Ancient Pagoda', 'Spain', 'Europe', 'Adventure', 149.60, 'Summer', 4.90, 1.33, 'Yes', 'https://images.pexels.com/photos/spain-pagoda-adventure.jpg'),
(187, 'Hidden Valley', 'Japan', 'Asia', 'Religious', 268.72, 'Spring', 4.80, 7.58, 'No', 'https://images.pexels.com/photos/japan-valley-religious.jpg'),
(188, 'Sacred Falls', 'Kenya', 'Africa', 'City', 98.14, 'Autumn', 4.40, 3.20, 'Yes', 'https://images.pexels.com/photos/kenya-waterfall-city.jpg'),
(189, 'Lush Canyon', 'Egypt', 'Africa', 'City', 219.79, 'Summer', 4.10, 5.92, 'No', 'https://images.pexels.com/photos/egypt-canyon-city.jpg'),
(190, 'Golden Canyon', 'Morocco', 'Africa', 'Adventure', 80.07, 'Summer', 4.70, 1.47, 'Yes', 'https://images.pexels.com/photos/morocco-canyon-adventure.jpg'),
(191, 'Sacred Ruins', 'Vietnam', 'Asia', 'Beach', 127.09, 'Summer', 4.30, 2.03, 'Yes', 'https://images.pexels.com/photos/vietnam-ruins-beach.jpg'),
(192, 'Sacred Plaza', 'China', 'Asia', 'Adventure', 120.38, 'Spring', 4.20, 2.15, 'No', 'https://images.pexels.com/photos/china-plaza-adventure.jpg'),
(193, 'Ancient Pagoda', 'South Africa', 'Africa', 'Religious', 30.00, 'Winter', 4.70, 9.83, 'No', 'https://images.pexels.com/photos/south-africa-pagoda-religious.jpg'),
(194, 'Hidden Temple', 'Egypt', 'Africa', 'Adventure', 138.05, 'Spring', 4.20, 9.71, 'Yes', 'https://images.pexels.com/photos/egypt-temple-adventure.jpg'),
(195, 'Ancient Falls', 'India', 'Asia', 'Adventure', 161.87, 'Winter', 4.60, 2.95, 'No', 'https://images.pexels.com/photos/india-waterfall-adventure.jpg'),
(196, 'Crystal Temple', 'New Zealand', 'Oceania', 'Religious', 175.05, 'Spring', 4.20, 9.33, 'Yes', 'https://images.pexels.com/photos/new-zealand-temple-religious.jpg'),
(197, 'Golden Beach', 'Greece', 'Europe', 'Adventure', 252.17, 'Summer', 5.00, 3.16, 'No', 'https://images.pexels.com/photos/greece-beach-adventure.jpg'),
(198, 'Serene Valley', 'Peru', 'South America', 'City', 116.87, 'Winter', 4.70, 3.57, 'No', 'https://images.pexels.com/photos/peru-valley-city.jpg'),
(199, 'Serene Falls', 'New Zealand', 'Oceania', 'City', 163.99, 'Winter', 3.70, 2.80, 'No', 'https://images.pexels.com/photos/new-zealand-waterfall-city.jpg'),
(200, 'Mystic Ruins', 'Germany', 'Europe', 'Nature', 162.99, 'Summer', 4.20, 3.24, 'Yes', 'https://images.pexels.com/photos/germany-ruins-nature.jpg');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `destinations`
--
ALTER TABLE `destinations`
  ADD PRIMARY KEY (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
