# Project Evaluation Criteria Released

Hello everyone,

The Project Evaluation Criteria Document has been shared below. Please go through it carefully to understand how your project submissions and GitHub repositories will be evaluated.

The document covers aspects such as:
- Repository structure
- Source code quality
- Documentation & README
- Build readiness
- Commit history
- Project completeness
- Originality & implementation effort

Important Note:
These evaluation criteria are applicable only for the project submission evaluation and not for the overall internship evaluation.

All students are strongly advised to review the document carefully and align their submissions accordingly.

— Team MindMatrix , Automated Project Evaluation Criteria

---

This document explains how student GitHub projects will be evaluated using an automated, liberal, evidence-based scoring system. The goal is to reward genuine effort, working implementation, clear documentation, and project completeness.

## Evaluation Overview

Every valid GitHub project starts with a base score of 60 marks. Additional marks are awarded based on measurable project signals such as source code, README quality, folder structure, commit history, build readiness, and originality.

**Important:** The evaluation is fully automated. Students should make sure their repositories are public, complete, well organized, and easy to understand from the files available on GitHub.

- **Base Score**: Valid, accessible repositories begin from 60 marks.
- **Final Score**: Final marks are calculated using automated checks and quality signals.
- **Build Check**: Projects may receive extra confidence marks if they build or compile successfully.
- **Feedback**: Automated feedback will include strengths and improvement suggestions.

## Final Grade Bands

| Score Range | Grade | Meaning |
| --- | --- | --- |
| 85-100 | Excellent | Strong, complete, well-structured project with good documentation and implementation quality. |
| 75-84 | Very Good | Good project with clear effort, useful features, and acceptable structure. |
| 60-74 | Good | Valid project with meaningful implementation, but some areas need improvement. |
| 45-59 | Basic Attempt | Some project evidence exists, but the submission is incomplete or weak. |
| 0-44 | Incomplete | Repository is inaccessible, empty, missing source code, or has very low project evidence. |

## Score Caps for Serious Issues

Even with a base score system, severe submission issues can limit the maximum possible score.

| Issue | Maximum Score | How to Avoid It |
| --- | --- | --- |
| Repository is inaccessible or invalid | 20 | Make the repository public and submit the correct GitHub repository URL. |
| Repository is empty | 25 | Push all project files, not just a placeholder repository. |
| No source code found | 35 | Include actual code files such as `.js`, `.py`, `.java`, `.html`, `.dart`, or similar. |
| Repository cannot be cloned | 45 | Ensure the repo is public and does not depend on missing private submodules or inaccessible files. |

## Detailed Evaluation Parameters

### 1. Repository Validity

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Repository accessibility | The GitHub repository can be opened and accessed. | Keep the repository public until evaluation is complete. |
| Meaningful files | The repository contains project files, not only images, PDFs, or empty folders. | Upload all source code, configuration files, assets, and documentation. |
| Source code presence | Recognized code files are present. | Do not submit only screenshots or presentations. The code must be in GitHub. |

### 2. Clone and Local Analysis

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Clone success | The repository can be cloned using Git. | Check that `git clone your-repo-url` works on another machine. |
| File scan success | The evaluator can read folders and files after cloning. | Avoid broken links, missing submodules, and private dependencies. |
| Reasonable repository size | The repo is not empty and not filled mainly with generated folders. | Do not upload `node_modules`, build folders, or unnecessary large generated files. |

### 3. README and Documentation

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| README exists | A file such as `README.md` is present. | Every project must include a README file. |
| Project description | The README has enough words to explain the project. | Write what problem the project solves and who it is for. |
| Setup instructions | Words such as install, setup, run, npm, pip, localhost, or similar are found. | Add exact commands required to install and run the project. |
| Feature or usage details | The README explains features, modules, usage, or functionality. | List the main features and explain how to use them. |
| Screenshots or demo | Screenshot images, demo links, deployed links, or video links are included. | Add screenshots or a short demo link if possible. |

**Recommended README format:** Project title, problem statement, features, tech stack, installation steps, run command, screenshots, demo link, folder structure, and future improvements.

### 4. Project Structure

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Multiple folders | The project has a clear folder structure. | Use folders such as `src`, `components`, `pages`, `backend`, `assets`, or `public`. |
| Configuration files | Files such as `package.json`, `requirements.txt`, `pom.xml`, `build.gradle`, or `pubspec.yaml` are present. | Include the dependency or configuration file required by your technology stack. |
| Separation of concerns | Code is separated into pages, components, routes, models, services, or similar modules. | Avoid putting the entire project into one very large file. |
| Organized assets | Images, CSS, static files, and media are placed in appropriate folders. | Use clean asset folders and meaningful file names. |

### 5. Code Volume and Effort

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Source file count | Enough meaningful code files are present. | Break the project into multiple useful files instead of one dump file. |
| Lines of code | The project has enough implementation volume for its type. | Complete the core features fully. Avoid submitting only starter template code. |
| Multiple modules or components | The project shows several logical parts. | Create reusable functions, components, models, pages, routes, or services. |
| Project-specific logic | The code appears customized for the submitted project idea. | Rename template content and add real functionality related to your problem statement. |

#### Project Type Code Volume Signals

| Project Type | Liberal Minimum Code Volume Signal |
| --- | --- |
| Static HTML/CSS/JavaScript | About 200 meaningful lines of code or more |
| React/Vite/Next frontend | About 300 meaningful lines of code or more |
| Backend/API project | About 250 meaningful lines of code or more |
| Full-stack project | About 500 meaningful lines of code or more |
| Java/Spring project | About 300 meaningful lines of code or more |
| Flutter/mobile project | About 400 meaningful lines of code or more |
| Python/ML/script project | About 200 meaningful lines of code or more |

### 6. Build and Run Confidence

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Project type detection | The evaluator can identify the stack, such as Node, Python, Java, Flutter, or web. | Use standard project files and folder names for your framework. |
| Dependency file | A dependency file is present. | Include files like `package.json`, `requirements.txt`, `pom.xml`, or `pubspec.yaml`. |
| Start or build script | The project has a recognizable run/build command. | For Node projects, include scripts such as `start`, `dev`, or `build` in `package.json`. |
| Build success | The project compiles or builds successfully when checked locally. | Test your project on a clean setup before submitting. |

> Build failure does not automatically fail the project. However, it can reduce build confidence marks. A project with clear code, structure, and documentation can still score well even if the build has minor issues.

### 7. Git Activity

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Commit count | More commits show development progress. | Commit regularly while building features. |
| Incremental development | The repository should not look like only one final upload. | Use meaningful commit messages such as `add login page`, `fix form validation`, or `update README`. |

#### Commit Count Signal

| Commit Count | Signal |
| --- | --- |
| 1 commit | Basic upload evidence |
| 2-4 commits | Some development progress |
| 5-10 commits | Good development progress |
| 10+ commits | Strong development history |

### 8. Originality and Template Risk

| Checked Parameter | What the System Looks For | Student Guidance |
| --- | --- | --- |
| Fork status | Whether the repository is a fork of another repository. | Submit your own repository unless you have clearly built original work on top of a base. |
| Custom project naming | The project name and README match the submitted idea. | Use project-specific names in README, components, pages, and files. |
| Default template text | The repository should not contain only untouched starter template content. | Remove default Vite, React, Next, Flutter, or other starter text and replace it with your project content. |
| Custom implementation | The project has enough custom code and domain-specific logic. | Add real features, forms, pages, validations, storage, APIs, or workflows related to your project. |

## What Students Should Submit

- A public GitHub repository URL.
- Complete source code.
- A clear `README.md`.
- Dependency/config files required to run the project.
- Screenshots or demo link if available.
- Meaningful commits showing development progress.

## Recommended Repository Checklist

| Checklist Item | Status Before Submission |
| --- | --- |
| Repository is public | Required |
| Correct repository link submitted | Required |
| Source code is present | Required |
| README explains the project | Strongly recommended |
| Setup and run commands are documented | Strongly recommended |
| Dependency files are included | Strongly recommended |
| Project builds or runs on a clean machine | Strongly recommended |
| Default template content has been replaced | Required for originality |
| Unnecessary generated folders are excluded | Recommended |

## Common Reasons for Low Scores

- The repository is private or inaccessible.
- The submitted link is a GitHub profile instead of a repository.
- The repository is empty or contains only a README.
- Source code files are missing.
- The project is only a default starter template.
- Dependency files are missing.
- No setup or run instructions are provided.
- The project cannot be cloned.
- Most project files are generated or unrelated.

> Final advice: A simple but complete project with clear code, a good README, proper structure, and working setup instructions will score better than a large but confusing or incomplete repository.
