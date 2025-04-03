package com.what3words.samples.googlemaps.compose.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Welcome to What3Words Maps Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "This demo shows how to integrate What3Words with Google Maps in Compose.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            onClick = { navController.navigate("first") }) {
            Text("Using W3WMapComponent")
        }


        Button(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            onClick = { navController.navigate("second") }) {
            Text("Using W3WDrawer with Existing Map")
        }
    }
}