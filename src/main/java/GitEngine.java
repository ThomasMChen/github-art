import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;


public class GitEngine {

    public String readToken(String filename) {
        String tokenStr;
        try {
            URL url = getClass().getResource("tokens.txt");
            tokenStr = readFileToString(new File(url.getPath()));
        } catch (IOException e) {
            return null;
        }


        return tokenStr;
    }

    public void updateLog(String repoFilePath, Git git) throws GitAPIException{

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        String date = sdf.format(cal.getTime());

        String data = date + " : " + UUID.randomUUID() + "\n";
        try {
            writeStringToFile(new File(repoFilePath), data, true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Successfuly Wrote: " + data);

        CommitCommand commit = git.commit().setAll(true).setMessage("Updated Log at " + date);
        RevCommit answer = commit.call();
        System.out.println("Committed " + answer.getId() + " " + answer.getFullMessage());

        PushCommand push = git.push();
        push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.readToken("tokens.txt"), ""));
        Iterable<PushResult> results = push.setRemote(git.getRepository().getConfig().getString("remote", "origin", "url" )).call();
        System.out.println(results.toString());
        System.out.println("Successfully Pushed");

    }

    public Git cloneRepo(String uri, String repoPath, String credPath) {
        Git git;
        try {
            git = Git.cloneRepository()
                    .setURI(uri)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.readToken(credPath),""))
                    .setDirectory(new File(repoPath))
                    .call();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return git;
    }

    public void deleteRepo(String repoPath) {
        try {
            deleteDirectory(new File(repoPath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        return;
    }

    public static void main(String[] args) {

        GitEngine testEngine = new GitEngine();

        Git gt = testEngine.cloneRepo("https://github.com/ThomasMChen/ga-target.git", "src/main/repo", "tokens.txt");
        try {
            testEngine.updateLog("src/main/repo/log.txt", gt);
        } catch (GitAPIException e) {
            System.out.println("Update Failed");
            e.printStackTrace();
        }

        testEngine.deleteRepo("src/main/repo");
    }
}
