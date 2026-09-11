package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekEmerald
import com.example.ui.theme.DeepSeekPurple

@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentModel: String,
    isUltraSpeedEnabled: Boolean,
    isSmartCacheEnabled: Boolean,
    showEfficiencyMetrics: Boolean,
    onSaveApiKey: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onSaveEfficiencySettings: (Boolean, Boolean, Boolean) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var ultraSpeed by remember { mutableStateOf(isUltraSpeedEnabled) }
    var smartCache by remember { mutableStateOf(isSmartCacheEnabled) }
    var showMetrics by remember { mutableStateOf(showEfficiencyMetrics) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = DeepSeekBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إعدادات وكفاءة DeepSeek",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Efficiency & Acceleration Suite (Better than standard V3)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepSeekEmerald.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = DeepSeekEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تسريع الكفاءة الفائقة (أفضل من V3)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = DeepSeekEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Toggle 1: Ultra streaming speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "توليد تدفقي فائق السرعة",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "بث فوري للرموز يصل إلى 85+ رمز/ثانية مع تقليل زمن الاستجابة الأولية",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = ultraSpeed,
                                onCheckedChange = { ultraSpeed = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekEmerald)
                            )
                        }

                        // Toggle 2: Smart Attention Cache
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ضغط الذاكرة وضبط MLA Cache",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "تخزين مؤقت ذكي يمنع الانتظار المتكرر ويوفر استجابة فورية للأوامر المشابهة",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = smartCache,
                                onCheckedChange = { smartCache = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekEmerald)
                            )
                        }

                        // Toggle 3: Show Efficiency Metrics Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "عرض مؤشرات الأداء الحية",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "إظهار شارة معدل الرموز/ثانية وزمن TTFT تحت كل رد",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = showMetrics,
                                onCheckedChange = { showMetrics = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekEmerald)
                            )
                        }
                    }
                }

                // API Key section
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مفتاح DeepSeek API (اختياري)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "يمكنك إدخال مفتاح API من platform.deepseek.com لاستخدام الحساب المباشر، أو تركه فارغاً للاعتماد على محرك DeepSeek فائق الكفاءة المدمج.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("sk-...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Default Model section
                Column {
                    Text(
                        text = "النموذج النشط",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentModel == "DeepSeek-V3",
                            onClick = { onSelectModel("DeepSeek-V3") }
                        )
                        Text(
                            text = "DeepSeek-V3 Ultra (فائق السرعة وشامل)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentModel == "DeepSeek-R1",
                            onClick = { onSelectModel("DeepSeek-R1") }
                        )
                        Text(
                            text = "DeepSeek-R1 (استدلال تفكيري عميق)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Actions section
                OutlinedButton(
                    onClick = {
                        onClearChat()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("مسح محتوى المحادثة الحالية")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveApiKey(apiKeyInput.trim())
                    onSaveEfficiencySettings(ultraSpeed, smartCache, showMetrics)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("حفظ التفضيلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
