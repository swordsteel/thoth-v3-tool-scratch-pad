package ltd.lulz.thoth.tool

import ltd.lulz.thoth.library.event.Event
import ltd.lulz.thoth.library.event.ThothLogger
import ltd.lulz.thoth.library.event.model.NoteType
import ltd.lulz.thoth.library.event.model.PromptEvent
import ltd.lulz.thoth.library.plugin.PluginType
import ltd.lulz.thoth.library.tool.ToolProvider
import ltd.lulz.thoth.library.tool.model.ThothTool
import ltd.lulz.thoth.library.tool.model.ThothToolProperty
import ltd.lulz.thoth.library.tool.model.ThothToolResponse
import ltd.lulz.thoth.library.tool.model.ThothToolType

private val logger = ThothLogger.logger("ToolScratchPad")

class ScratchPad : ToolProvider {

    override val type: PluginType = PluginType.TOOL_PROVIDER

    override val id = "scratch-pad"

    override val toolType: ThothToolType = ThothToolType.TOOL

    override val name: String = "scratch_pad"

    override val description: String = "A personal scratch pad for temporary notes during tasks. " +
        "Notes appear in your system prompt on subsequent turns, helping you remember important context, " +
        "decisions, and findings. Use write to add notes, append to add more, and clear to start fresh."

    override fun getDetails(): String = """
        ## Tool: scratch_pad
        
        A lightweight scratch pad for the agent to keep temporary notes during complex tasks.
        
        ### Actions
        - `write`   → Add a new note / line to the scratch pad
        - `append`  → Same as write (adds to the end)
        - `clear`   → Wipe the scratch pad (use only when starting fresh)
        
        ### What you should write (suggestions):
        - Key decisions you made
        - Important findings or observations
        - Todo / next steps list
        - User preferences or constraints you discovered
        - Technical details you might need later (IDs, versions, paths, etc.)
        - Partial results or intermediate outputs
        - Questions you want to answer later
        - "I noticed that..." or "Remember that..."
        
        ### Examples
        
        Write a note:
        ```json
        {"action": "write", "content": "User wants dark mode by default and prefers Tailwind over plain CSS"}
        ```
        
        Add multiple points:
        ```json
        {"action": "write", "content": "TODO:\n1. Fix UUID v7 sorting in getFragments\n2. Add recent mode to MemorySearch\n3. Test with 20+ fragments"}
        ```
        
        Append to note:
        ```json
        {"action": "append", "content": "User wants dark mode by default and prefers Tailwind over plain CSS"}
        ```
        
        Clear when starting a completely new task:
        ```json
        {"action": "clear"}
        ```
    """.trimIndent()

    override fun getSchema(): ThothTool = ThothTool(
        name = name,
        description = description,
        parameters = listOf(
            ThothToolProperty(
                name = "action",
                type = "string",
                description = "Action to perform: write, append, clear",
                enum = listOf("write", "append", "clear"),
                required = true,
            ),
            ThothToolProperty(
                name = "content",
                type = "string",
                description = "The note or text to write (required for write/append)",
                required = false,
            ),
        ),
    )

    override suspend fun initialize(
        config: String?,
    ) {
    }

    override suspend fun execute(
        args: Map<String, String>,
    ): ThothToolResponse {
        logger.trace { "Executing $name with args: $args" }

        val action = args["action"]?.trim()?.lowercase()
            ?: return ThothToolResponse("Error: 'action' parameter is required (write, append, read, clear)")

        return when (action) {
            "write" -> checkText(args["content"]) { content ->
                val update = PromptEvent.Note(
                    name = name,
                    type = NoteType.WRITE,
                    data = content,
                )
                Event.prompt.tryEmit(update)
            }

            "append" -> checkText(args["content"]) { content ->
                val update = PromptEvent.Note(
                    name = name,
                    type = NoteType.APPEND,
                    data = content,
                )
                Event.prompt.tryEmit(update)
            }

            "clear" -> {
                val update = PromptEvent.Note(
                    name = name,
                    type = NoteType.CLEAR,
                )
                Event.prompt.tryEmit(update)
                ThothToolResponse("Scratch pad cleared.")
            }

            else -> ThothToolResponse("Error: Unknown action '$action'. Use: write, append, read, clear.")
        }
    }

    private fun checkText(
        context: String?,
        function: (String) -> Boolean,
    ): ThothToolResponse {
        val content = context?.trim()
            ?: return ThothToolResponse("Error: 'content' is required when writing to scratch pad.")
        return if (content.isNotBlank()) {
            function(context)
            ThothToolResponse("✓ Note added to scratch pad.")
        } else {
            ThothToolResponse("Error: Content cannot be empty.")
        }
    }
}
