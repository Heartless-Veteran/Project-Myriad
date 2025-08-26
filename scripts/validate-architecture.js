#!/usr/bin/env node

/**
 * Architecture Validation Script for Project Myriad
 * Validates Clean Architecture principles and layer separation
 */

const fs = require('fs');
const path = require('path');

console.log('🏗️  Architecture Validation for Project Myriad');
console.log('===============================================\n');

class ArchitectureValidator {
    constructor() {
        this.srcPath = 'app/src/main/kotlin/com/heartlessveteran/myriad';
        this.violations = [];
        this.warnings = [];
        this.stats = {
            totalFiles: 0,
            dataLayerFiles: 0,
            domainLayerFiles: 0,
            uiLayerFiles: 0,
            networkLayerFiles: 0,
            utilFiles: 0
        };
    }

    validate() {
        console.log('1. Checking layer structure...');
        this.validateLayerStructure();
        
        console.log('2. Validating dependency directions...');
        this.validateDependencyFlow();
        
        console.log('3. Checking naming conventions...');
        this.validateNamingConventions();
        
        console.log('4. Validating package organization...');
        this.validatePackageOrganization();
        
        this.printResults();
    }

    validateLayerStructure() {
        const expectedDirs = [
            'data',
            'domain', 
            'ui',
            'network',
            'utils'
        ];

        expectedDirs.forEach(dir => {
            const dirPath = path.join(this.srcPath, dir);
            if (fs.existsSync(dirPath)) {
                console.log(`✅ Found ${dir} layer`);
                this.countFilesInLayer(dirPath, dir);
            } else {
                this.violations.push(`Missing ${dir} layer directory`);
            }
        });

        // Validate data layer structure
        if (fs.existsSync(path.join(this.srcPath, 'data'))) {
            const dataSubdirs = ['database', 'repository', 'di'];
            dataSubdirs.forEach(subdir => {
                const subdirPath = path.join(this.srcPath, 'data', subdir);
                if (!fs.existsSync(subdirPath)) {
                    this.warnings.push(`Data layer missing ${subdir} subdirectory`);
                }
            });
        }

        // Validate UI layer structure  
        if (fs.existsSync(path.join(this.srcPath, 'ui'))) {
            const uiSubdirs = ['screens', 'navigation', 'theme', 'viewmodel'];
            uiSubdirs.forEach(subdir => {
                const subdirPath = path.join(this.srcPath, 'ui', subdir);
                if (!fs.existsSync(subdirPath)) {
                    this.warnings.push(`UI layer missing ${subdir} subdirectory`);
                }
            });
        }
    }

    countFilesInLayer(dirPath, layerName) {
        try {
            const files = this.getKotlinFiles(dirPath);
            this.stats.totalFiles += files.length;
            this.stats[`${layerName}LayerFiles`] = files.length;
            console.log(`  📁 ${layerName}: ${files.length} Kotlin files`);
        } catch (error) {
            this.warnings.push(`Could not count files in ${layerName}: ${error.message}`);
        }
    }

    validateDependencyFlow() {
        // Check for improper dependencies (UI -> Data, Domain -> UI, etc.)
        const uiFiles = this.getKotlinFiles(path.join(this.srcPath, 'ui'));
        const dataFiles = this.getKotlinFiles(path.join(this.srcPath, 'data'));
        
        uiFiles.forEach(file => {
            const content = fs.readFileSync(file, 'utf8');
            
            // UI should not import data directly
            if (content.includes('import com.heartlessveteran.myriad.data') && 
                !content.includes('import com.heartlessveteran.myriad.data.repository')) {
                this.violations.push(`UI file ${path.basename(file)} imports data layer directly`);
            }
            
            // Check for proper ViewModel usage
            if (content.includes('@Composable') && content.includes('viewModel()')) {
                const fileName = path.basename(file);
                if (!fileName.includes('Screen') && !fileName.includes('Component')) {
                    this.warnings.push(`${fileName} uses viewModel() but name doesn't suggest it's a screen`);
                }
            }
        });

        // Domain should not depend on UI or Data implementations
        if (fs.existsSync(path.join(this.srcPath, 'domain'))) {
            const domainFiles = this.getKotlinFiles(path.join(this.srcPath, 'domain'));
            domainFiles.forEach(file => {
                const content = fs.readFileSync(file, 'utf8');
                
                if (content.includes('import com.heartlessveteran.myriad.ui')) {
                    this.violations.push(`Domain file ${path.basename(file)} imports UI layer`);
                }
                
                if (content.includes('import com.heartlessveteran.myriad.data') && 
                    !content.includes('import com.heartlessveteran.myriad.domain')) {
                    this.violations.push(`Domain file ${path.basename(file)} imports data implementations`);
                }
            });
        }
    }

    validateNamingConventions() {
        // Check ViewModels
        const viewModelFiles = this.findFilesWithPattern('ViewModel.kt');
        viewModelFiles.forEach(file => {
            const content = fs.readFileSync(file, 'utf8');
            if (!content.includes(': ViewModel()') && !content.includes(': AndroidViewModel(')) {
                this.violations.push(`${path.basename(file)} has ViewModel suffix but doesn't extend ViewModel`);
            }
        });

        // Check Repositories
        const repositoryFiles = this.findFilesWithPattern('Repository.kt');
        repositoryFiles.forEach(file => {
            const fileName = path.basename(file);
            if (fileName.endsWith('RepositoryImpl.kt')) {
                // Implementation files should be in data layer
                if (!file.includes('/data/')) {
                    this.violations.push(`${fileName} (implementation) should be in data layer`);
                }
            } else if (fileName.endsWith('Repository.kt')) {
                // Interface files should be in domain layer
                if (!file.includes('/domain/')) {
                    this.violations.push(`${fileName} (interface) should be in domain layer`);
                }
            }
        });

        // Check Composables
        const composeFiles = this.getKotlinFiles(path.join(this.srcPath, 'ui'));
        composeFiles.forEach(file => {
            const content = fs.readFileSync(file, 'utf8');
            const composableCount = (content.match(/@Composable/g) || []).length;
            if (composableCount > 0) {
                const fileName = path.basename(file, '.kt');
                if (!fileName.endsWith('Screen') && !fileName.endsWith('Component') && !fileName.endsWith('s')) {
                    this.warnings.push(`${fileName}.kt contains @Composable but doesn't follow naming convention`);
                }
            }
        });
    }

    validatePackageOrganization() {
        // Check if package declarations match directory structure
        const allFiles = this.getKotlinFiles(this.srcPath);
        
        allFiles.forEach(file => {
            const content = fs.readFileSync(file, 'utf8');
            const packageMatch = content.match(/^package\s+([^\s\n]+)/m);
            
            if (packageMatch) {
                const declaredPackage = packageMatch[1];
                const expectedPackage = this.getExpectedPackageFromPath(file);
                
                if (declaredPackage !== expectedPackage) {
                    this.violations.push(`Package mismatch in ${path.basename(file)}: declared '${declaredPackage}', expected '${expectedPackage}'`);
                }
            } else {
                this.violations.push(`Missing package declaration in ${path.basename(file)}`);
            }
        });
    }

    getKotlinFiles(dir) {
        if (!fs.existsSync(dir)) return [];
        
        let files = [];
        const items = fs.readdirSync(dir);
        
        items.forEach(item => {
            const itemPath = path.join(dir, item);
            const stat = fs.statSync(itemPath);
            
            if (stat.isDirectory()) {
                files = files.concat(this.getKotlinFiles(itemPath));
            } else if (item.endsWith('.kt')) {
                files.push(itemPath);
            }
        });
        
        return files;
    }

    findFilesWithPattern(pattern) {
        const allFiles = this.getKotlinFiles(this.srcPath);
        return allFiles.filter(file => path.basename(file).includes(pattern));
    }

    getExpectedPackageFromPath(filePath) {
        const relativePath = path.relative(this.srcPath, path.dirname(filePath));
        const packageParts = relativePath.split(path.sep).filter(part => part !== '.');
        return `com.heartlessveteran.myriad${packageParts.length > 0 ? '.' + packageParts.join('.') : ''}`;
    }

    printResults() {
        console.log('\n📊 Architecture Validation Results');
        console.log('==================================');
        
        console.log('\n📈 Statistics:');
        console.log(`Total Kotlin files: ${this.stats.totalFiles}`);
        console.log(`Data layer files: ${this.stats.dataLayerFiles}`);
        console.log(`Domain layer files: ${this.stats.domainLayerFiles}`);
        console.log(`UI layer files: ${this.stats.uiLayerFiles}`);
        console.log(`Network layer files: ${this.stats.networkLayerFiles}`);
        console.log(`Utility files: ${this.stats.utilFiles}`);

        if (this.violations.length === 0) {
            console.log('\n✅ No architecture violations found!');
        } else {
            console.log('\n❌ Architecture Violations:');
            this.violations.forEach((violation, index) => {
                console.log(`${index + 1}. ${violation}`);
            });
        }

        if (this.warnings.length > 0) {
            console.log('\n⚠️  Warnings:');
            this.warnings.forEach((warning, index) => {
                console.log(`${index + 1}. ${warning}`);
            });
        }

        console.log(`\n🏁 Validation Complete: ${this.violations.length} violations, ${this.warnings.length} warnings`);
        
        // Exit with error code if violations found
        if (this.violations.length > 0) {
            process.exit(1);
        }
    }
}

// Run validation
if (require.main === module) {
    const validator = new ArchitectureValidator();
    validator.validate();
}

module.exports = ArchitectureValidator;