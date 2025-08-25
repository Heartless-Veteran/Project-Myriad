import React, {useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
  Alert,
} from 'react-native';
import Button from '../components/Button';
import {UserPreferences} from '../types';

const SettingsScreen: React.FC = () => {
  const [preferences, setPreferences] = useState<UserPreferences>({
    theme: 'dark',
    language: 'en',
    aiFeatures: {
      ocrTranslation: true,
      recommendations: true,
      artStyleMatching: false,
    },
    readingDirection: 'ltr',
    autoSync: true,
  });

  const updatePreference = <K extends keyof UserPreferences>(
    key: K,
    value: UserPreferences[K],
  ) => {
    setPreferences(prev => ({...prev, [key]: value}));
  };

  const updateAIFeature = (feature: keyof UserPreferences['aiFeatures'], value: boolean) => {
    setPreferences(prev => ({
      ...prev,
      aiFeatures: {
        ...prev.aiFeatures,
        [feature]: value,
      },
    }));
  };

  const handleClearCache = () => {
    Alert.alert(
      'Clear Cache',
      'This will remove all cached images and temporary files. Continue?',
      [
        {text: 'Cancel', style: 'cancel'},
        {
          text: 'Clear',
          style: 'destructive',
          onPress: () => {
            // TODO: Implement cache clearing
            console.log('Cache cleared');
          },
        },
      ],
    );
  };

  const handleExportLibrary = () => {
    // TODO: Implement library export
    console.log('Export library functionality to be implemented');
  };

  const handleImportLibrary = () => {
    // TODO: Implement library import
    console.log('Import library functionality to be implemented');
  };

  return (
    <View style={styles.container}>
      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Appearance</Text>

          <View style={styles.settingRow}>
            <Text style={styles.settingLabel}>Theme</Text>
            <View style={styles.themeOptions}>
              {(['light', 'dark', 'auto'] as const).map(theme => (
                <TouchableOpacity
                  key={theme}
                  style={[
                    styles.themeOption,
                    preferences.theme === theme && styles.activeThemeOption,
                  ]}
                  onPress={() => updatePreference('theme', theme)}>
                  <Text
                    style={[
                      styles.themeOptionText,
                      preferences.theme === theme && styles.activeThemeOptionText,
                    ]}>
                    {theme.charAt(0).toUpperCase() + theme.slice(1)}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>

          <View style={styles.settingRow}>
            <Text style={styles.settingLabel}>Reading Direction</Text>
            <View style={styles.themeOptions}>
              {(['ltr', 'rtl', 'vertical'] as const).map(direction => (
                <TouchableOpacity
                  key={direction}
                  style={[
                    styles.themeOption,
                    preferences.readingDirection === direction && styles.activeThemeOption,
                  ]}
                  onPress={() => updatePreference('readingDirection', direction)}>
                  <Text
                    style={[
                      styles.themeOptionText,
                      preferences.readingDirection === direction && styles.activeThemeOptionText,
                    ]}>
                    {direction.toUpperCase()}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>

          <View style={styles.settingRow}>
            <Text style={styles.settingLabel}>Reading Mode</Text>
            <View style={styles.readingModeOptions}>
              {(['single', 'double', 'continuous', 'webtoon', 'fit-width', 'fit-height'] as const).map(mode => (
                <TouchableOpacity
                  key={mode}
                  style={[
                    styles.readingModeOption,
                    preferences.readingMode === mode && styles.activeReadingModeOption,
                  ]}
                  onPress={() => updatePreference('readingMode', mode)}>
                  <Text
                    style={[
                      styles.readingModeOptionText,
                      preferences.readingMode === mode && styles.activeReadingModeOptionText,
                    ]}>
                    {mode.charAt(0).toUpperCase() + mode.slice(1).replace('-', ' ')}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>AI Features</Text>

          <View style={styles.settingRow}>
            <View style={styles.settingInfo}>
              <Text style={styles.settingLabel}>OCR Translation</Text>
              <Text style={styles.settingDescription}>
                Translate manga text using AI-powered OCR
              </Text>
            </View>
            <Switch
              value={preferences.aiFeatures.ocrTranslation}
              onValueChange={value => updateAIFeature('ocrTranslation', value)}
              trackColor={{false: '#444', true: '#007BFF'}}
              thumbColor="#fff"
            />
          </View>

          <View style={styles.settingRow}>
            <View style={styles.settingInfo}>
              <Text style={styles.settingLabel}>Smart Recommendations</Text>
              <Text style={styles.settingDescription}>
                Get personalized content recommendations
              </Text>
            </View>
            <Switch
              value={preferences.aiFeatures.recommendations}
              onValueChange={value => updateAIFeature('recommendations', value)}
              trackColor={{false: '#444', true: '#007BFF'}}
              thumbColor="#fff"
            />
          </View>

          <View style={styles.settingRow}>
            <View style={styles.settingInfo}>
              <Text style={styles.settingLabel}>Art Style Matching</Text>
              <Text style={styles.settingDescription}>
                Find similar content based on art style
              </Text>
            </View>
            <Switch
              value={preferences.aiFeatures.artStyleMatching}
              onValueChange={value => updateAIFeature('artStyleMatching', value)}
              trackColor={{false: '#444', true: '#007BFF'}}
              thumbColor="#fff"
            />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Data & Storage</Text>

          <View style={styles.settingRow}>
            <View style={styles.settingInfo}>
              <Text style={styles.settingLabel}>Auto Sync</Text>
              <Text style={styles.settingDescription}>
                Automatically sync reading progress across devices
              </Text>
            </View>
            <Switch
              value={preferences.autoSync}
              onValueChange={value => updatePreference('autoSync', value)}
              trackColor={{false: '#444', true: '#007BFF'}}
              thumbColor="#fff"
            />
          </View>

          <Button
            title="Clear Cache"
            onPress={handleClearCache}
            style={[styles.actionButton, styles.destructiveButton]}
          />

          <Button
            title="Export Library"
            onPress={handleExportLibrary}
            style={styles.actionButton}
          />

          <Button
            title="Import Library"
            onPress={handleImportLibrary}
            style={styles.actionButton}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>About</Text>
          <Text style={styles.aboutText}>
            Project Myriad - The Definitive Manga and Anime Platform
          </Text>
          <Text style={styles.versionText}>Version 1.0.0</Text>
        </View>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#121212',
  },
  content: {
    flex: 1,
    padding: 20,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 16,
  },
  settingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  settingInfo: {
    flex: 1,
    marginRight: 16,
  },
  settingLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  settingDescription: {
    fontSize: 14,
    color: '#CCCCCC',
  },
  themeOptions: {
    flexDirection: 'row',
  },
  themeOption: {
    backgroundColor: '#2c2c2c',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    marginLeft: 8,
  },
  activeThemeOption: {
    backgroundColor: '#007BFF',
  },
  themeOptionText: {
    color: '#CCCCCC',
    fontSize: 14,
    fontWeight: '500',
  },
  activeThemeOptionText: {
    color: '#FFFFFF',
  },
  readingModeOptions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    maxWidth: 200,
    justifyContent: 'flex-end',
  },
  readingModeOption: {
    backgroundColor: '#2c2c2c',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    margin: 2,
    minWidth: 60,
    alignItems: 'center',
  },
  activeReadingModeOption: {
    backgroundColor: '#007BFF',
  },
  readingModeOptionText: {
    color: '#CCCCCC',
    fontSize: 11,
    fontWeight: '500',
    textAlign: 'center',
  },
  activeReadingModeOptionText: {
    color: '#FFFFFF',
  },
  actionButton: {
    marginTop: 12,
  },
  destructiveButton: {
    backgroundColor: '#DC3545',
  },
  aboutText: {
    color: '#CCCCCC',
    fontSize: 16,
    marginBottom: 8,
  },
  versionText: {
    color: '#666',
    fontSize: 14,
  },
});

export default SettingsScreen;
