import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Button from '../components/Button';
import Card from '../components/Card';

const HomeScreen: React.FC = () => {
  const navigation = useNavigation();

  const handleImportMedia = () => {
    navigation.navigate('Library');
  };

  const handleBrowseOnline = () => {
    navigation.navigate('Browse');
  };

  const handleAICore = () => {
    navigation.navigate('AICore');
  };

  const recentItems = [
    {
      id: '1',
      title: 'Attack on Titan',
      imageUrl: 'https://example.com/covers/attack-on-titan.jpg',
      tags: ['Action', 'Drama'],
    },
    {
      id: '2',
      title: 'One Piece',
      imageUrl: 'https://example.com/covers/one-piece.jpg',
      tags: ['Adventure', 'Comedy'],
    },
  ];

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <Text style={styles.title}>Project Myriad</Text>
          <Text style={styles.subtitle}>The Definitive Manga and Anime Platform</Text>
        </View>

        <View style={styles.quickActions}>
          <Text style={styles.sectionTitle}>Quick Actions</Text>
          <View style={styles.buttonContainer}>
            <Button
              title="Import Local Media"
              onPress={handleImportMedia}
              style={styles.actionButton}
            />
            <Button
              title="Browse Online"
              onPress={handleBrowseOnline}
              style={[styles.actionButton, styles.secondaryButton]}
            />
          </View>
        </View>

        <View style={styles.recentSection}>
          <Text style={styles.sectionTitle}>Recently Added</Text>
          {recentItems.map(item => (
            <Card
              key={item.id}
              title={item.title}
              imageUrl={item.imageUrl}
              tags={item.tags}
              onPress={() => console.log(`Pressed ${item.title}`)}
            />
          ))}
        </View>

        <View style={styles.featuresSection}>
          <Text style={styles.sectionTitle}>Features</Text>
          <TouchableOpacity style={styles.featureCard} onPress={handleAICore}>
            <Text style={styles.featureTitle}>AI Core</Text>
            <Text style={styles.featureDescription}>
              Access AI-powered tools for your media
            </Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.featureCard} onPress={() => navigation.navigate('Settings')}>
            <Text style={styles.featureTitle}>App Settings</Text>
            <Text style={styles.featureDescription}>
              Configure application preferences and options
            </Text>
          </TouchableOpacity>
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
  scrollView: {
    flex: 1,
  },
  header: {
    padding: 20,
    alignItems: 'center',
    backgroundColor: '#1a1a1a',
    marginBottom: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#CCCCCC',
    textAlign: 'center',
  },
  quickActions: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 16,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  actionButton: {
    flex: 1,
    marginHorizontal: 8,
  },
  secondaryButton: {
    backgroundColor: '#444',
  },
  recentSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  featuresSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  featureCard: {
    backgroundColor: '#2c2c2c',
    padding: 16,
    borderRadius: 8,
    marginBottom: 12,
  },
  featureTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  featureDescription: {
    fontSize: 14,
    color: '#CCCCCC',
  },
});

export default HomeScreen;
