from fabric.api import run, cd
from fabric.contrib.project import rsync_project

def deploy():
    "Deploy the Rest api & website to production"
    with cd('/home/protected/jtime/JTime-rest'):
        run('git stash && git pull && git stash pop')
        run('npm install .')
        rsync_project(
            remote_dir = '/home/protected/jtime/JTime-rest/client/jtime-website',
            local_dir = 'JTime-rest/client/jtime-website/dist')
        run('touch restart-rest-api.flag')
