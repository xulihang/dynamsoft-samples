B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=4.2
@EndOfDesignText@
Sub Class_Globals
	Private fx As JFX
End Sub

'Initializes the object. You can NOT add parameters to this method!
Public Sub Initialize() As String
	Log("Initializing plugin " & GetNiceName)
	' Here return a key to prevent running unauthorized plugins
	Return "MyKey"
End Sub

' must be available
public Sub GetNiceName() As String
	Return "BarcodeOCR"
End Sub

' must be available
public Sub Run(Tag As String, Params As Map) As ResumableSub
	Log("run"&Params)
	Select Tag
		Case "getParams"
			Dim paramsList As List
			paramsList.Initialize
			paramsList.Add("license")
			Return paramsList
		Case "getText"
			wait for (GetText(Params.Get("img"),Params.Get("lang"))) complete (result As String)
			Return result
		Case "getTextWithLocation"
			wait for (GetTextWithLocation(Params.Get("img"),Params.Get("lang"))) complete (regions As List)
			Return regions
	End Select
	Return ""
End Sub

Sub GetText(img As B4XBitmap, lang As String) As ResumableSub
	wait for (ocr(img,lang)) complete (result As Map)
	Return result.GetDefault("Text","")
End Sub

Sub GetTextWithLocation(img As B4XBitmap, lang As String) As ResumableSub
	wait for (ocr(img,lang)) complete (result As Map)
	If result.ContainsKey("boxes") Then
		Dim Boxes As List=result.Get("boxes")
		Return Boxes
	Else
		Dim Boxes As List
		Boxes.Initialize
		Return Boxes
	End If
End Sub

Sub LangHasSpace(lang As String) As Boolean
	If lang.StartsWith("ch") Or lang.StartsWith("jp") Then
		Return False
	Else
		Return True
	End If
End Sub

Sub ocr(img As B4XBitmap, Lang As String) As ResumableSub
	Dim license As String="t0068NQAAAJYBYfmF8T9A4FyRD4gw30Kx9VtWdhk4M7K8OgvmtsAySfNNO0Fi3uIBlvoHUBWLJB4MQ1bUt9k8v+TrrG1cXio="
	Try
		If File.Exists(File.DirApp,"preferences.conf") Then
			Dim preferencesMap As Map = readJsonAsMap(File.ReadString(File.DirApp,"preferences.conf"))
			license=getMap("Barcode",getMap("api",preferencesMap)).GetDefault("license",license)
		End If
	Catch
		Log(LastException)
		Return ""
	End Try

	Dim result As Map
	result.Initialize
	Dim out As OutputStream
	out=File.OpenOutput(File.DirApp,"image.jpg",False)
	img.WriteToStream(out,"100","JPEG")
	out.Close
	Dim sh As Shell
	sh.Initialize("sh","java",Array("-jar","BarcodeReaderCLI.jar","image.jpg",license))
	sh.WorkingDirectory=File.DirApp
	sh.Run(10000)
	wait for sh_ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)
	Log(StdOut)
	Log(StdErr)
	If Success And ExitCode = 0 Then
		If File.Exists(File.DirApp,"image.jpg-out.json") Then
			result=Data2Map(File.ReadString(File.DirApp,"image.jpg-out.json"))
		End If
	End If
	Return result
End Sub

Sub Data2Map(data As String) As Map
	Dim json As JSONParser
	json.Initialize(data)
	Dim results As List=json.NextArray
	Dim boxes As List
	boxes.Initialize
	Dim sb As StringBuilder
	sb.Initialize
	For Each barcodeResult As Map In results
		Dim box As Map
		box.Initialize
		box.Put("text",barcodeResult.Get("barcodeText"))
		sb.Append(box.Get("text")).Append(CRLF)
		Dim localizationResult As Map=barcodeResult.Get("localizationResult")
		Dim points As List =localizationResult.Get("resultPoints")
		Dim minX,maxX,minY,maxY As Int
		Dim index As Int=0
		For Each point As Map In points
			If index=0 Then
				minX=point.Get("x")
				minY=point.Get("y")
			End If
			minX=Min(minX,point.Get("x"))
			minY=Min(minY,point.Get("y"))
			maxX=Max(maxX,point.Get("x"))
			maxY=Max(maxY,point.Get("y"))
			index=index+1
		Next
		box.Put("X",minX)
		box.Put("Y",minY)
		box.Put("width",maxX-minX)
		box.Put("height",maxY-minY)
		boxes.Add(box)
	Next
	Dim result As Map
	result.Initialize
	result.Put("boxes",boxes)
	result.Put("Text",sb.ToString.Trim)
	Return result
End Sub

Sub getMap(key As String,parentmap As Map) As Map
	Return parentmap.Get(key)
End Sub

Sub readJsonAsMap(s As String) As Map
	Dim json As JSONParser
	json.Initialize(s)
	Return json.NextObject
End Sub

