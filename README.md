# MS Gestion Receveur

## 📋 Description

**MS Gestion Receveur** est un microservice Spring Boot qui gère les opérations des receveurs dans l'écosystème FoodNexus. Il permet aux receveurs de gérer leurs stocks, déclarer leurs besoins, recevoir des dons et évaluer les transactions.

## 🚀 Fonctionnalités Principales

- **📦 Gestion des Stocks** : Déclaration et suivi des capacités de stockage
- **🎯 Gestion des Besoins** : Création et monitoring des besoins en produits
- **🤝 Gestion des Donations** : Réception et matching des dons avec les besoins
- **⭐ Système de Notation** : Évaluation des dons reçus (1-5 étoiles)
- **🚨 Alertes Automatiques** : Notifications avant expiration des besoins
- **🤖 Prédictions IA** : Prévisions des besoins futurs et recommandations

## 🛠️ Technologies

- **Backend** : Spring Boot 3.x
- **Base de données** : PostgreSQL avec JPA/Hibernate
- **Sécurité** : Spring Security avec Keycloak
- **Documentation** : OpenAPI 3.0 (Swagger)

## 📊 Base de Données

### Entités Principales

- **Stockage** : Capacités et caractéristiques des espaces de stockage
- **Besoin** : Demandes de produits avec dates d'expiration
- **Notation** : Évaluations des dons reçus (note + commentaire)
- **Alerte** : Notifications automatiques avec niveaux d'urgence

## 🔌 API REST

### Endpoints Clés

#### Stockage
- `GET /api/receveur/stockages` - Lister tous les stockages
- `POST /api/receveur/stockages` - Créer un stockage
- `PUT /api/receveur/stockages/{id}` - Mettre à jour un stockage

#### Besoins
- `GET /api/receveur/besoins` - Lister tous les besoins
- `POST /api/receveur/besoins` - Créer un besoin
- `GET /api/receveur/besoins/satisfaits/{userId}` - Besoins satisfaits

#### Notations
- `GET /api/receveur/notations/{userId}` - Notations d'un utilisateur
- `POST /api/receveur/notations` - Créer une notation
- `GET /api/receveur/notations/score-moyen/{userId}` - Score moyen

#### Alertes
- `GET /api/receveur/alertes/non-lues/{userId}` - Alertes non lues
- `POST /api/receveur/alertes/generer` - Générer alertes (scheduler)

#### Prédictions IA
- `GET /api/receveur/predictions/{userId}` - Prédire besoins
- `GET /api/receveur/predictions/{userId}/recommendations` - Recommandations

## 🛠️ Installation

### Prérequis
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

### Démarrage
```bash
# Cloner le repository
git clone https://github.com/FoodNexus/ms_gestionReceveur.git
cd ms_gestionReceveur

# Configurer la base de données dans application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/foodnexus_receveur

# Lancer l'application
mvn spring-boot:run
```

### Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/foodnexus_receveur
spring.datasource.username=your_username
spring.datasource.password=your_password

keycloak.auth-server-url=http://localhost:8080/auth
keycloak.realm=foodnexus
keycloak.resource=ms-receveur
```

## 🧪 Tests

```bash
# Lancer les tests
mvn test

# Tests d'intégration
mvn verify -P integration-tests
```

## 🚀 Déploiement

### Docker
```bash
# Build
mvn clean package

# Docker
docker build -t ms-receveur .
docker run -p 8084:8084 ms-receveur
```

### Port par défaut
- **API** : `8084`
- **Documentation** : `http://localhost:8084/swagger-ui.html`

## 📈 Monitoring

- **Health Check** : `/actuator/health`
- **Métriques** : `/actuator/metrics`
- **Info** : `/actuator/info`

## 🔒 Sécurité

- **Authentification** : Keycloak OAuth 2.0
- **Rôles** : RECEIVER, ADMIN
- **Protection** : CORS configuré pour le frontend

## 🤝 Contribution

1. Fork le repository
2. Créer une branche feature
3. Commit avec messages clairs
4. Push et créer une Pull Request

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/FoodNexus/ms_gestionReceveur/issues)
- **Documentation** : [Wiki](https://github.com/FoodNexus/ms_gestionReceveur/wiki)

---

**FoodNexus** - *Connecter les donneurs et les receveurs pour réduire le gaspillage alimentaire* 🌱

**Version** : 1.0.0 | **Licence** : MIT
