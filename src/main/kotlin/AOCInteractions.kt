@file:Suppress("KotlinConstantConditions") // year < 2015

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path

const val year: Int = -1
private val http: HttpClient = HttpClient.newHttpClient()

fun downloadInput(day: UByte) {
	if(year < 2015)
		throw IllegalStateException("Invalid year $year (expected >= 2015)")

	// If file exists - return
	// If file doesn't exist - download the input and save as file
	// >>> $session is stored in another kotlin file, excluded from git (for obvious reasons)
	if(!Files.exists(Path.of("inputs")))
		Files.createDirectory(Path.of("inputs"))

	val path: Path = Path.of("inputs/$day.in")
	if(Files.exists(path)) return

	println("[Downloading input for day $day]")
	val request: HttpRequest = HttpRequest
		.newBuilder(URI("https://adventofcode.com/$year/day/$day/input"))
		.header("Cookie", "session=$session")
		.build()
	val response: HttpResponse<String> = http.send(request, HttpResponse.BodyHandlers.ofString())
	assert(response.statusCode() == 200) { return@assert "Status code for downloading input for day $day was ${response.statusCode()} (probably update your session token)" }
	Files.createFile(path)
	val file: File = path.toFile()
	assert(file.canWrite()) { return@assert "Cannot write to input file" }
	file.writeText(response.body())
}

fun submitSolution(day: UByte, part: Int, sol: String) {
	if(year < 2015)
		throw IllegalStateException("Invalid year $year (expected >= 2015)")

	print("Do you want to submit `$sol` as the solution to day $day part $part? [yN] ")
	if(readln().dropLastWhile { it == '\n' } != "y") return

	val request: HttpRequest = HttpRequest
		.newBuilder(URI("https://adventofcode.com/$year/day/$day/answer"))
		.POST(HttpRequest.BodyPublishers.ofString("level=$part&answer=${sol.replace("=", "%3D")}", Charsets.UTF_8))
		.header("Cookie", "session=$session")
		.header("Content-Type", "application/x-www-form-urlencoded")
		.build()

	val response: HttpResponse<String> = http.send(request, HttpResponse.BodyHandlers.ofString())
	assert(response.statusCode() == 200) { return@assert "[!] Status code was ${response.statusCode()}" }
	val resp: String = response.body()
		.replaceBefore("<p>", "") // Remove anything before the part that is interesting to us
		.replaceAfter("</p>", "") // Remove anything after the part that is interesting to us
		.replace(Regex("</?.*?>"), "") // Remove all unnecesarry tags to avoid clutter
		.replace("  ", " ") // Replace a small mistake in the formatting
		.replace(Regex(" If you're stuck,.+?subreddit."), "") // Too lonnggg
	println(resp)
}