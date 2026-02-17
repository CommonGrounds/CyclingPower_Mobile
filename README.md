# Cycle Power is cycling computer for mobiles

[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21+-blue?logo=java&logoColor=white)](https://openjfx.io)
[![AtlantaFX](https://img.shields.io/badge/AtlantaFX-2.x-4C8BF5?logoColor=white)](https://github.com/mkpaz/atlantafx)
[![GitHub License](https://img.shields.io/github/license/CommonGrounds/CyclingPower_Mobile)](https://github.com/CommonGrounds/CyclingPower_Mobile)
[![Stars](https://img.shields.io/github/stars/tvojusername/projekt?style=social)](https://github.com/tvojusername/projekt/stargazers)

> **Moderni JavaFX desktop klijent sa AtlantaFX temom** – sve ono što si ikada želeo, a nije postojalo.

## Sadržaj
- [Prikaz aplikacije](#prikaz-aplikacije)
- [Funkcionalnosti](#funkcionalnosti)
- [Tehnologije](#tehnologije)
- [Kako pokrenuti](#kako-pokrenuti)
- [Build](#build)
- [Instalacija](#instalacija)
- [Konfiguracija](#konfiguracija)
- [Doprinos](#doprinos)
- [Licenca](#licenca)

## Prikaz aplikacije

| Light tema                          | Dark tema                           |
|-------------------------------------|-------------------------------------|
| ![Light](screenshots/light.png)     | ![Dark](screenshots/dark.png)       |

https://github.com/user-attachments/assets/... (GitHub novi video/embed)

## Funkcionalnosti

- Podebljan, kurziv i `kod u liniji`
- Liste sa podstavkama
  - Podebljan tekst
  - Boje preko HTML-a ako treba: <span style="color:#e91e63">pink tekst</span>
- Tabele (vidi dole)
- Checkbox zadaci
  - [x] Napravljen login ekran
  - [ ] Dodaj dark mode switcher
- Citati, upozorenja i info blokovi

> [!NOTE]
> Ovo je korisna informacija koju ne želiš da propustiš.

> [!TIP]
> Pro tip: koristi AtlantaFX + Cupertina tema za najlepši izgled.

> [!WARNING]
> Java 21+ obavezna!

> [!CAUTION]
> Ne briši `src/main/resources/themes/` folder.

## Tehnologije

| Tehnologija       | Verzija     | Napomena                          |
|-------------------|-------------|-----------------------------------|
| Java              | 21+         | records, sealed classes, pattern matching |
| JavaFX            | 23          | modulski ili nemodulski           |
| AtlantaFX         | 2.0+        | prelepe teme i komponente        |
| Gluon Attach      | najnoviji   | za dodatne servise (opciono)     |
| Maven / Gradle    | —           | oba podržana                      |

## Kako pokrenuti (3 najčešća načina)

### 1. IntelliJ IDEA (najlakše)
```bash
git clone https://github.com/tvojusername/projekt.git
# Otvori folder u IntelliJ → Run → Main klasa
```

### 2. Maven
```bash
./mvnw clean javafx:run
```

### 3. Gradle
```bash
./gradlew run
```

## Build

```bash
# Maven – izvršni JAR sa svim zavisnostima
./mvnw clean package

# Gradle – native image (potreban GraalVM)
./gradlew nativeCompile
```

Izvršni fajl će biti u:
- `target/projekt-1.0.jar`
- `build/native/nativeCompile/projekt`

## Instalacija

### Windows
1. Preuzmi `projekt-setup.exe` iz [Releases](https://github.com/tvojusername/projekt/releases)
2. Pokreni instalater → Next → Next → Finish

### macOS (dmg + pkg) i Linux (AppImage) – dolaze uskoro

## Konfiguracija

Konfiguracija se nalazi u `config/application.yml`:
```yaml
app:
  theme: cupertino-light      # cupertino-dark, primer, nord-light, nord-dark…
  language: sr                # sr, en, de…
  check-updates: true
```

## Doprinos (Contributing)

Hvala što želiš da doprineseš!

1. Fork-uj projekat
2. Napravi feature branch (`git checkout -b feature/cool-stvar`)
3. Commit-uj izmene (`git commit -m "Add cool-stvar"`)
4. Push-uj branch (`git push origin feature/cool-stvar`)
5. Otvori Pull Request

> Molimo te da pratiš [Conventional Commits](https://www.conventionalcommits.org) i da formatiraš kod sa Google Java Format-om.

## Autori i zahvalnice

- **Ti** – glavni autor
- [@mkpaz](https://github.com/mkpaz) – AtlantaFX (hvala za najlepše JavaFX teme!)
- Svi kontributori – vi ste sjajni

## Licenca

Ovaj projekat je licenciran pod **MIT License** – pogledaj [LICENSE](LICENSE) fajl za detalje.

---

Made with JavaFX & AtlantaFX

---
*Poslednje ažuriranje: 21. novembar 2025.*


### Zašto je ovaj šablon najbolji za IntelliJ IDEA + GitHub

| Funkcija                     | Radi u IntelliJ IDEA preview? | Radi na GitHub-u? |
|------------------------------|-------------------------------|-------------------|
| Badges (shields.io)          | Yes                           | Yes               |
| GitHub Alerts (`> [!NOTE]`)  | Yes (od 2024+)                | Yes               |
| Tabele                       | Yes                           | Yes               |
| Task liste `[x]`             | Yes                           | Yes               |
| Slike i video embed          | Yes                           | Yes               |
| HTML boje i stilovi          | Yes                           | Yes (ograničeno)  |
| Emoji                        | Yes                           | Yes               |

### Bonus: korisni IntelliJ Markdown plugin-i (ako ih još nemaš)
- **Markdown Navigator Enhanced** (plaćen, ali vredi svaki cent)
- **GitHub Markdown Preview** (besplatan)

Samo kopiraj ceo tekst iznad, sačuvaj kao `README.md` i biće ti najlepši i najprofesionalniji JavaFX/AtlantaFX README na Balkanu

Ako želiš varijante (npr. sa Dependabot badge-ovima, CI statusom, code coverage-om, download brojačem…), samo reci – imam ih još 5 spremljene.