package com.example.washintonwearos.presentation

//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.wearable.Wearable
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class SeccondActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private var transcriptionNodeId: String? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.deployButton.setOnClickListener {
//            lifecycleScope.launch(Dispatchers.IO){
//                transcriptionNodeId = getNodes().first()?.also { nodeId->
//                    val sendTask: Task<*> = Wearable.getMessageClient(applicationContext).sendMessage(
//                        nodeId,
//                        MESSAGE_PATH,
//                        "deploy".toByteArray() //send your desired information here
//                    ).apply {
//                        addOnSuccessListener { Log.d(TAG, "OnSuccess") }
//                        addOnFailureListener { Log.d(TAG, "OnFailure") }
//                    }
//                }
//            }
//        }
//    }
//
//    companion object{
//        private const val TAG = "MainWearActivity"
//        private const val MESSAGE_PATH = "/deploy"
//    }
//}
//
//
