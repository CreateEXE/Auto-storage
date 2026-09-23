package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.FileMetadata
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StorageBreakdownWidget(
    storageBreakdown: Map<String, Long>,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    
    LaunchedEffect(storageBreakdown) {
        if (storageBreakdown.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(storageBreakdown.values.map { it.toFloat() / 1024 / 1024 }) // Convert to MB
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(SteelSurfaceContainer)
                .padding(16.dp)
        ) {
            Text("Storage Breakdown (MB)", style = MaterialTheme.typography.titleSmall, color = TextSilver)
            Spacer(modifier = Modifier.height(8.dp))
            if (storageBreakdown.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No entities indexed yet", color = TextSteelMuted, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.height(200.dp)
                )
            }
        }
    }
}
